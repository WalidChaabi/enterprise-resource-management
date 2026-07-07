package com.company.erm.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.dto.settings.BillingPortalResponseDTO;
import com.company.erm.dto.settings.BillingSettingsResponseDTO;
import com.company.erm.dto.settings.EntitlementsResponseDTO;
import com.company.erm.dto.settings.NotificationPreferencesResponseDTO;
import com.company.erm.dto.settings.NotificationPreferencesUpdateDTO;
import com.company.erm.dto.settings.OrganizationSettingsResponseDTO;
import com.company.erm.dto.settings.OrganizationSettingsUpdateDTO;
import com.company.erm.dto.settings.ProfileSettingsResponseDTO;
import com.company.erm.dto.settings.ProfileSettingsUpdateDTO;
import com.company.erm.dto.settings.ShortcutDefinitionDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.entity.UserNotificationPreferences;
import com.company.erm.exception.BusinessException;
import com.company.erm.mapper.RoleMapper;
import com.company.erm.config.SettingsShortcutsProperties;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.UserNotificationPreferencesRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.security.RoleCode;

@Service
@Transactional
public class SettingsService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserNotificationPreferencesRepository notificationPreferencesRepository;
    private final SettingsShortcutsProperties shortcutsProperties;
    private final StripeBillingService stripeBillingService;
    private final PermissionChecker permissionChecker;
    private final EntitlementService entitlementService;

    public SettingsService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            RoleMapper roleMapper,
            UserNotificationPreferencesRepository notificationPreferencesRepository,
            SettingsShortcutsProperties shortcutsProperties,
            StripeBillingService stripeBillingService,
            PermissionChecker permissionChecker,
            EntitlementService entitlementService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.shortcutsProperties = shortcutsProperties;
        this.stripeBillingService = stripeBillingService;
        this.permissionChecker = permissionChecker;
        this.entitlementService = entitlementService;
    }

    @Transactional(readOnly = true)
    public ProfileSettingsResponseDTO getProfile(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        ProfileSettingsResponseDTO dto = new ProfileSettingsResponseDTO();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    public ProfileSettingsResponseDTO updateProfile(String currentUserEmail, ProfileSettingsUpdateDTO updateDTO) {
        User user = getUserByEmail(currentUserEmail);

        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        userRepository.save(user);

        return getProfile(currentUserEmail);
    }

    @Transactional(readOnly = true)
    public OrganizationSettingsResponseDTO getOrganization(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Organization org = user.getOrganization();
        OrganizationSettingsResponseDTO dto = new OrganizationSettingsResponseDTO();
        dto.setId(org.getId());
        dto.setName(org.getName());
        dto.setCode(org.getCode());
        dto.setActive(org.isActive());
        return dto;
    }

    public OrganizationSettingsResponseDTO updateOrganization(String currentUserEmail, OrganizationSettingsUpdateDTO updateDTO) {
        User user = getUserByEmail(currentUserEmail);
        Organization org = user.getOrganization();

        org.setName(updateDTO.getName());
        if (updateDTO.getCode() != null && !updateDTO.getCode().isBlank()) {
            if (!org.getCode().equalsIgnoreCase(updateDTO.getCode())) {
                organizationRepository.findByCode(updateDTO.getCode()).ifPresent(existing -> {
                    throw new BusinessException("ORG_CODE_EXISTS", "Organization code already exists", HttpStatus.CONFLICT);
                });
            }
            org.setCode(updateDTO.getCode());
        }
        organizationRepository.save(org);

        return getOrganization(currentUserEmail);
    }

    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getRoles(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return roleRepository.findByOrganizationOrScope(user.getOrganization(), "GLOBAL").stream()
            .map(roleMapper::toResponseDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public BillingSettingsResponseDTO getBilling(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Organization org = user.getOrganization();
        long seatCount = userRepository.countByOrganizationIdAndActiveTrue(org.getId());
        return stripeBillingService.getBillingSummary(org, seatCount);
    }

    public BillingPortalResponseDTO createBillingPortalSession(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Organization org = user.getOrganization();
        permissionChecker.requireRole(org.getId(), currentUserEmail, RoleCode.ORG_ADMIN);
        return stripeBillingService.createPortalSession(org);
    }

    public BillingPortalResponseDTO createBillingCheckoutSession(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Organization org = user.getOrganization();
        permissionChecker.requireRole(org.getId(), currentUserEmail, RoleCode.ORG_ADMIN);
        long seatCount = userRepository.countByOrganizationIdAndActiveTrue(org.getId());
        return stripeBillingService.createCheckoutSession(org, seatCount);
    }

    @Transactional(readOnly = true)
    public EntitlementsResponseDTO getEntitlements(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return entitlementService.getEntitlements(user.getOrganization());
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResponseDTO getNotificationPreferences(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        UserNotificationPreferences prefs = getOrCreatePreferences(user);
        NotificationPreferencesResponseDTO dto = new NotificationPreferencesResponseDTO();
        dto.setEmailEnabled(prefs.isEmailEnabled());
        dto.setInAppEnabled(prefs.isInAppEnabled());
        return dto;
    }

    public NotificationPreferencesResponseDTO updateNotificationPreferences(
            String currentUserEmail,
            NotificationPreferencesUpdateDTO updateDTO) {
        User user = getUserByEmail(currentUserEmail);
        UserNotificationPreferences prefs = getOrCreatePreferences(user);
        prefs.setEmailEnabled(updateDTO.getEmailEnabled());
        prefs.setInAppEnabled(updateDTO.getInAppEnabled());
        notificationPreferencesRepository.save(prefs);
        return getNotificationPreferences(currentUserEmail);
    }

    @Transactional(readOnly = true)
    public List<ShortcutDefinitionDTO> getShortcuts(String currentUserEmail) {
        getUserByEmail(currentUserEmail);
        return shortcutsProperties.getShortcuts().stream()
            .map(s -> new ShortcutDefinitionDTO(s.getKey(), s.getAction()))
            .toList();
    }

    private User getUserByEmail(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
    }

    private UserNotificationPreferences getOrCreatePreferences(User user) {
        return notificationPreferencesRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                UserNotificationPreferences prefs = new UserNotificationPreferences();
                prefs.setUser(user);
                prefs.setEmailEnabled(true);
                prefs.setInAppEnabled(true);
                return notificationPreferencesRepository.save(prefs);
            });
    }
}
