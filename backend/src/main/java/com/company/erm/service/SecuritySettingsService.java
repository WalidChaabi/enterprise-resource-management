package com.company.erm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.MfaBackupCodesResponseDTO;
import com.company.erm.dto.MfaEnrollConfirmDTO;
import com.company.erm.dto.MfaSetupResponseDTO;
import com.company.erm.dto.settings.SecuritySettingsResponseDTO;
import com.company.erm.dto.settings.SecuritySettingsUpdateDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.security.RoleCode;

@Service
@Transactional
public class SecuritySettingsService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PermissionChecker permissionChecker;
    private final MfaService mfaService;
    private final SsoAuthService ssoAuthService;

    public SecuritySettingsService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PermissionChecker permissionChecker,
            MfaService mfaService,
            SsoAuthService ssoAuthService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.permissionChecker = permissionChecker;
        this.mfaService = mfaService;
        this.ssoAuthService = ssoAuthService;
    }

    @Transactional(readOnly = true)
    public SecuritySettingsResponseDTO getSecuritySettings(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Organization organization = user.getOrganization();
        SecuritySettingsResponseDTO dto = new SecuritySettingsResponseDTO();
        dto.setSsoEnabled(organization.isSsoEnabled());
        dto.setRequireMfa(organization.isRequireMfa());
        dto.setSsoProvider(organization.getSsoProvider());
        dto.setUserMfaEnabled(mfaService.hasMfaEnrolled(user.getId()));
        dto.setUnusedBackupCodes(mfaService.countUnusedBackupCodes(user));
        dto.setAvailableSsoProviders(ssoAuthService.resolveProvidersForOrganization(organization));
        return dto;
    }

    public SecuritySettingsResponseDTO updateSecuritySettings(String currentUserEmail, SecuritySettingsUpdateDTO updateDTO) {
        User user = getUserByEmail(currentUserEmail);
        permissionChecker.requireRole(user.getOrganization().getId(), currentUserEmail, RoleCode.ORG_ADMIN);
        Organization organization = organizationRepository.findById(user.getOrganization().getId())
                .orElseThrow();
        if (updateDTO.getSsoEnabled() != null) {
            organization.setSsoEnabled(updateDTO.getSsoEnabled());
        }
        if (updateDTO.getRequireMfa() != null) {
            organization.setRequireMfa(updateDTO.getRequireMfa());
        }
        if (updateDTO.getSsoProvider() != null) {
            organization.setSsoProvider(updateDTO.getSsoProvider().isBlank() ? null : updateDTO.getSsoProvider());
        }
        organizationRepository.save(organization);
        return getSecuritySettings(currentUserEmail);
    }

    public MfaSetupResponseDTO beginMfaEnrollment(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return mfaService.beginEnrollment(user);
    }

    public MfaBackupCodesResponseDTO confirmMfaEnrollment(String currentUserEmail, MfaEnrollConfirmDTO confirmDTO) {
        User user = getUserByEmail(currentUserEmail);
        return mfaService.confirmEnrollment(user, confirmDTO.secret(), confirmDTO.code());
    }

    public void disableMfa(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        mfaService.disableMfa(user);
    }

    public MfaBackupCodesResponseDTO regenerateBackupCodes(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return mfaService.regenerateBackupCodes(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.company.erm.exception.BusinessException(
                        "NOT_FOUND", "User not found", org.springframework.http.HttpStatus.NOT_FOUND));
    }
}
