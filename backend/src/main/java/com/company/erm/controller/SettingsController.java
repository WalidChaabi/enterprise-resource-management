package com.company.erm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.company.erm.service.SecuritySettingsService;
import com.company.erm.service.SettingsService;

import jakarta.validation.Valid;

/**
 * REST controller for settings endpoints.
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Settings", description = "Profile, organization, security, and billing settings")
@RestController
@RequestMapping("/api/settings")
@PreAuthorize("isAuthenticated()")
public class SettingsController {

    private final SettingsService settingsService;
    private final SecuritySettingsService securitySettingsService;

    public SettingsController(SettingsService settingsService, SecuritySettingsService securitySettingsService) {
        this.settingsService = settingsService;
        this.securitySettingsService = securitySettingsService;
    }

    @Operation(summary = "Get Profile")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/profile")
    public ResponseEntity<ProfileSettingsResponseDTO> getProfile(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getProfile(currentUserEmail));
    }

    @Operation(summary = "Update Profile")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/profile")
    public ResponseEntity<ProfileSettingsResponseDTO> updateProfile(
            @Valid @RequestBody ProfileSettingsUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateProfile(currentUserEmail, updateDTO));
    }

    @Operation(summary = "Get Organization")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/organization")
    public ResponseEntity<OrganizationSettingsResponseDTO> getOrganization(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getOrganization(currentUserEmail));
    }

    @Operation(summary = "Update Organization")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/organization")
    public ResponseEntity<OrganizationSettingsResponseDTO> updateOrganization(
            @Valid @RequestBody OrganizationSettingsUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateOrganization(currentUserEmail, updateDTO));
    }

    @Operation(summary = "Get Roles")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getRoles(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getRoles(currentUserEmail));
    }

    @Operation(summary = "Get Billing")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/billing")
    public ResponseEntity<BillingSettingsResponseDTO> getBilling(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getBilling(currentUserEmail));
    }

    @Operation(summary = "Create Billing Portal Session")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/billing/portal")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN')")
    public ResponseEntity<BillingPortalResponseDTO> createBillingPortalSession(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.createBillingPortalSession(currentUserEmail));
    }

    @Operation(summary = "Create Billing Checkout Session")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/billing/checkout")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN')")
    public ResponseEntity<BillingPortalResponseDTO> createBillingCheckoutSession(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.createBillingCheckoutSession(currentUserEmail));
    }

    @Operation(summary = "Get Entitlements")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/entitlements")
    public ResponseEntity<EntitlementsResponseDTO> getEntitlements(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getEntitlements(currentUserEmail));
    }

    @Operation(summary = "Get Notification Preferences")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponseDTO> getNotificationPreferences(
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getNotificationPreferences(currentUserEmail));
    }

    @Operation(summary = "Update Notification Preferences")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponseDTO> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferencesUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateNotificationPreferences(currentUserEmail, updateDTO));
    }

    @Operation(summary = "Get Shortcuts")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/shortcuts")
    public ResponseEntity<List<ShortcutDefinitionDTO>> getShortcuts(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getShortcuts(currentUserEmail));
    }

    @Operation(summary = "API operation")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/security")
    public ResponseEntity<com.company.erm.dto.settings.SecuritySettingsResponseDTO> getSecurity(
            Authentication authentication) {
        return ResponseEntity.ok(securitySettingsService.getSecuritySettings(authentication.getName()));
    }

    @Operation(summary = "API operation")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/security")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN')")
    public ResponseEntity<com.company.erm.dto.settings.SecuritySettingsResponseDTO> updateSecurity(
            @Valid @RequestBody com.company.erm.dto.settings.SecuritySettingsUpdateDTO updateDTO,
            Authentication authentication) {
        return ResponseEntity.ok(securitySettingsService.updateSecuritySettings(authentication.getName(), updateDTO));
    }

    @Operation(summary = "API operation")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/security/mfa/setup")
    public ResponseEntity<com.company.erm.dto.MfaSetupResponseDTO> beginMfaSetup(Authentication authentication) {
        return ResponseEntity.ok(securitySettingsService.beginMfaEnrollment(authentication.getName()));
    }

    @Operation(summary = "API operation")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/security/mfa/confirm")
    public ResponseEntity<com.company.erm.dto.MfaBackupCodesResponseDTO> confirmMfaSetup(
            @Valid @RequestBody com.company.erm.dto.MfaEnrollConfirmDTO confirmDTO,
            Authentication authentication) {
        return ResponseEntity.ok(securitySettingsService.confirmMfaEnrollment(authentication.getName(), confirmDTO));
    }

    @Operation(summary = "Disable Mfa")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/security/mfa/disable")
    public ResponseEntity<Void> disableMfa(Authentication authentication) {
        securitySettingsService.disableMfa(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "API operation")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/security/mfa/backup-codes")
    public ResponseEntity<com.company.erm.dto.MfaBackupCodesResponseDTO> regenerateBackupCodes(
            Authentication authentication) {
        return ResponseEntity.ok(securitySettingsService.regenerateBackupCodes(authentication.getName()));
    }
}
