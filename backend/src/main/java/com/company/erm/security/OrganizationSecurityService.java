package com.company.erm.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Canonical org-scoped authorization API for multi-tenant access control.
 * <p>
 * Membership and roles are resolved from {@code user_roles} (not {@code users.organization_id}).
 * Use in {@code @PreAuthorize} via SpEL, e.g.
 * {@code @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN')")}.
 * <p>
 * See {@code docs/ADR-org-scoped-authorization.md}.
 */
@Service("organizationSecurity")
public class OrganizationSecurityService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuthCookieService authCookieService;

    public OrganizationSecurityService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            JwtTokenProvider tokenProvider,
            AuthCookieService authCookieService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.tokenProvider = tokenProvider;
        this.authCookieService = authCookieService;
    }

    // --- SpEL entry points (current authenticated user) ---

    public boolean hasOrgAccess(UUID organizationId) {
        return hasOrgMembership(organizationId);
    }

    public boolean hasOrgMembership(UUID organizationId) {
        return hasOrgMembership(resolveCurrentUserEmail(), organizationId);
    }

    public boolean hasAnyOrgRole(UUID organizationId, String... roleCodes) {
        return hasAnyOrgRole(resolveCurrentUserEmail(), organizationId, roleCodes);
    }

    public void requireOrgMembership(UUID organizationId) {
        requireOrgMembership(resolveCurrentUserEmail(), organizationId);
    }

    public void requireAnyOrgRole(UUID organizationId, String... roleCodes) {
        requireAnyOrgRole(resolveCurrentUserEmail(), organizationId, roleCodes);
    }

    public void requireAnyOrgRole(UUID organizationId, RoleCode... roles) {
        requireAnyOrgRole(
                organizationId,
                Arrays.stream(roles).map(RoleCode::code).toArray(String[]::new));
    }

    public void requireOrgRole(UUID organizationId, String roleCode) {
        requireAnyOrgRole(organizationId, roleCode);
    }

    public void requireOrgRole(UUID organizationId, RoleCode role) {
        requireAnyOrgRole(organizationId, role.code());
    }

    public void requireOrgRole(String userEmail, UUID organizationId, String roleCode) {
        requireAnyOrgRole(userEmail, organizationId, roleCode);
    }

    public void requireOrgRole(String userEmail, UUID organizationId, RoleCode role) {
        requireAnyOrgRole(userEmail, organizationId, role.code());
    }

    /**
     * Active organization from the JWT in the current request (cookie or Bearer header).
     */
    public UUID resolveJwtOrganizationId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return resolveJwtOrganizationId(attributes.getRequest());
    }

    public UUID requireJwtOrganizationId() {
        UUID organizationId = resolveJwtOrganizationId();
        if (organizationId == null) {
            throw new AccessDeniedException("Active organization not found in session");
        }
        return organizationId;
    }

    public boolean hasAnyOrgRoleInJwtOrganization(String... roleCodes) {
        UUID organizationId = resolveJwtOrganizationId();
        if (organizationId == null) {
            return false;
        }
        return hasAnyOrgRole(organizationId, roleCodes);
    }

    // --- Programmatic entry points (explicit user email, for services) ---

    public boolean hasOrgMembership(String userEmail, UUID organizationId) {
        User user = loadUser(userEmail);
        return !rolesInOrganization(user.getId(), organizationId).isEmpty();
    }

    public boolean hasAnyOrgRole(String userEmail, UUID organizationId, String... roleCodes) {
        if (roleCodes.length == 0) {
            return false;
        }
        Set<String> required = Set.of(roleCodes);
        User user = loadUser(userEmail);
        return rolesInOrganization(user.getId(), organizationId).stream()
                .anyMatch(ur -> required.contains(ur.getRole().getCode()));
    }

    public boolean hasAnyOrgRole(String userEmail, UUID organizationId, RoleCode... roles) {
        return hasAnyOrgRole(
                userEmail,
                organizationId,
                Arrays.stream(roles).map(RoleCode::code).toArray(String[]::new));
    }

    public void requireOrgMembership(String userEmail, UUID organizationId) {
        if (!hasOrgMembership(userEmail, organizationId)) {
            throw new AccessDeniedException("User does not have access to organization: " + organizationId);
        }
    }

    public void requireAnyOrgRole(String userEmail, UUID organizationId, String... roleCodes) {
        requireOrgMembership(userEmail, organizationId);
        if (!hasAnyOrgRole(userEmail, organizationId, roleCodes)) {
            throw new AccessDeniedException(
                    "User does not have required role in organization: " + String.join(", ", roleCodes));
        }
    }

    public void requireAnyOrgRole(String userEmail, UUID organizationId, RoleCode... roles) {
        requireAnyOrgRole(
                userEmail,
                organizationId,
                Arrays.stream(roles).map(RoleCode::code).toArray(String[]::new));
    }

    private List<UserRole> rolesInOrganization(UUID userId, UUID organizationId) {
        return userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
    }

    private User loadUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    private String resolveCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        return authentication.getName();
    }

    private UUID resolveJwtOrganizationId(HttpServletRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token) || !tokenProvider.validateToken(token)) {
            return null;
        }
        return tokenProvider.getOrganizationIdFromToken(token);
    }

    private String resolveToken(HttpServletRequest request) {
        String cookieToken = authCookieService.readAccessToken(request);
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Returns role codes assigned to the user in the given organization.
     * Intended for diagnostics and future request-scoped caching (Task 1.2).
     */
    public Set<String> getOrgRoleCodes(String userEmail, UUID organizationId) {
        User user = loadUser(userEmail);
        return rolesInOrganization(user.getId(), organizationId).stream()
                .map(ur -> ur.getRole().getCode())
                .collect(Collectors.toSet());
    }
}
