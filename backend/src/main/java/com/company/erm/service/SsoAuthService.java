package com.company.erm.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.OAuth2Properties;
import com.company.erm.dto.SsoProvidersResponseDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.entity.UserIdentityLink;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserIdentityLinkRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.AuthService.LoginResult;

@Service
@Transactional
public class SsoAuthService {

    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_AZURE = "azure";
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of(PROVIDER_GOOGLE, PROVIDER_AZURE);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserIdentityLinkRepository identityLinkRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Properties oauth2Properties;
    private final Environment environment;

    public SsoAuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            UserIdentityLinkRepository identityLinkRepository,
            AuthService authService,
            PasswordEncoder passwordEncoder,
            OAuth2Properties oauth2Properties,
            Environment environment) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.identityLinkRepository = identityLinkRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.oauth2Properties = oauth2Properties;
        this.environment = environment;
    }

    @Transactional(readOnly = true)
    public SsoProvidersResponseDTO resolveProvidersForEmail(String email) {
        if (!oauth2Properties.isEnabled() || email == null || email.isBlank()) {
            return new SsoProvidersResponseDTO(List.of());
        }
        return userRepository.findByEmail(email.trim())
                .map(user -> new SsoProvidersResponseDTO(resolveProvidersForOrganization(user.getOrganization())))
                .orElseGet(() -> new SsoProvidersResponseDTO(List.of()));
    }

    @Transactional(readOnly = true)
    public List<String> resolveProvidersForOrganization(Organization organization) {
        if (!oauth2Properties.isEnabled() || organization == null || !organization.isSsoEnabled()) {
            return List.of();
        }
        if (organization.getSsoProvider() == null || organization.getSsoProvider().isBlank()
                || "ALL".equalsIgnoreCase(organization.getSsoProvider())) {
            return configuredProviders();
        }
        String provider = organization.getSsoProvider().toLowerCase(Locale.ROOT);
        if (configuredProviders().contains(provider)) {
            return List.of(provider);
        }
        return List.of();
    }

    public record SsoLoginResult(boolean mfaRequired, String mfaToken, AuthSession session) {
    }

    public SsoLoginResult completeOidcLogin(String registrationId, String subject, String email, String givenName, String familyName) {
        String provider = normalizeProvider(registrationId);
        User user = resolveUser(provider, subject, email, givenName, familyName);
        if (!user.isActive()) {
            throw new BusinessException("AUTH_ERROR", "User account is inactive", HttpStatus.UNAUTHORIZED);
        }
        Organization organization = user.getOrganization();
        if (!organization.isSsoEnabled()) {
            throw new BusinessException("AUTH_ERROR", "SSO is not enabled for this organization", HttpStatus.FORBIDDEN);
        }
        if (!isProviderAllowed(organization, provider)) {
            throw new BusinessException("AUTH_ERROR", "SSO provider is not allowed for this organization", HttpStatus.FORBIDDEN);
        }
        LoginResult login = authService.beginAuthenticatedSession(user);
        if (login.mfaRequired()) {
            return new SsoLoginResult(true, login.mfaToken(), null);
        }
        return new SsoLoginResult(false, null, login.session());
    }

    private User resolveUser(String provider, String subject, String email, String givenName, String familyName) {
        Optional<UserIdentityLink> existingLink = identityLinkRepository.findByProviderAndSubject(provider, subject);
        if (existingLink.isPresent()) {
            return existingLink.get().getUser();
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException("AUTH_ERROR", "Email is required for SSO login", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseGet(() -> provisionJitUser(email.trim(), givenName, familyName));
        UserIdentityLink link = new UserIdentityLink();
        link.setUser(user);
        link.setProvider(provider);
        link.setSubject(subject);
        link.setEmail(email.trim());
        identityLinkRepository.save(link);
        return user;
    }

    private User provisionJitUser(String email, String givenName, String familyName) {
        // JIT: attach to first active organization with SSO enabled (pilot policy)
        Organization organization = organizationRepository.findAll().stream()
                .filter(Organization::isActive)
                .filter(Organization::isSsoEnabled)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "AUTH_ERROR", "No organization is configured for SSO provisioning", HttpStatus.FORBIDDEN));
        User user = new User();
        user.setOrganization(organization);
        user.setEmail(email);
        user.setFirstName(givenName != null && !givenName.isBlank() ? givenName : email.split("@")[0]);
        user.setLastName(familyName != null && !familyName.isBlank() ? familyName : "User");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActive(true);
        return userRepository.save(user);
    }

    private boolean isProviderAllowed(Organization organization, String provider) {
        return resolveProvidersForOrganization(organization).contains(provider);
    }

    private List<String> configuredProviders() {
        return SUPPORTED_PROVIDERS.stream()
                .filter(this::isProviderConfigured)
                .toList();
    }

    private boolean isProviderConfigured(String provider) {
        String envKey = switch (provider) {
            case "google" -> "GOOGLE_CLIENT_ID";
            case "azure" -> "AZURE_CLIENT_ID";
            default -> null;
        };
        return envKey != null
                && environment.getProperty(envKey) != null
                && !environment.getProperty(envKey, "").isBlank();
    }

    private String normalizeProvider(String registrationId) {
        String provider = registrationId == null ? "" : registrationId.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_PROVIDERS.contains(provider)) {
            throw new BusinessException("AUTH_ERROR", "Unsupported SSO provider", HttpStatus.BAD_REQUEST);
        }
        return provider;
    }
}
