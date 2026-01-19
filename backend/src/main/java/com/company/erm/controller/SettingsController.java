package com.company.erm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.dto.settings.BillingSettingsResponseDTO;
import com.company.erm.dto.settings.NotificationPreferencesResponseDTO;
import com.company.erm.dto.settings.NotificationPreferencesUpdateDTO;
import com.company.erm.dto.settings.OrganizationSettingsResponseDTO;
import com.company.erm.dto.settings.OrganizationSettingsUpdateDTO;
import com.company.erm.dto.settings.ProfileSettingsResponseDTO;
import com.company.erm.dto.settings.ProfileSettingsUpdateDTO;
import com.company.erm.dto.settings.ShortcutDefinitionDTO;
import com.company.erm.service.SettingsService;

import jakarta.validation.Valid;

/**
 * REST controller for settings endpoints.
 */
@RestController
@RequestMapping("/api/settings")
@PreAuthorize("isAuthenticated()")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileSettingsResponseDTO> getProfile(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getProfile(currentUserEmail));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileSettingsResponseDTO> updateProfile(
            @Valid @RequestBody ProfileSettingsUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateProfile(currentUserEmail, updateDTO));
    }

    @GetMapping("/organization")
    public ResponseEntity<OrganizationSettingsResponseDTO> getOrganization(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getOrganization(currentUserEmail));
    }

    @PutMapping("/organization")
    public ResponseEntity<OrganizationSettingsResponseDTO> updateOrganization(
            @Valid @RequestBody OrganizationSettingsUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateOrganization(currentUserEmail, updateDTO));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getRoles(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getRoles(currentUserEmail));
    }

    @GetMapping("/billing")
    public ResponseEntity<BillingSettingsResponseDTO> getBilling(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getBilling(currentUserEmail));
    }

    @GetMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponseDTO> getNotificationPreferences(
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getNotificationPreferences(currentUserEmail));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponseDTO> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferencesUpdateDTO updateDTO,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateNotificationPreferences(currentUserEmail, updateDTO));
    }

    @GetMapping("/shortcuts")
    public ResponseEntity<List<ShortcutDefinitionDTO>> getShortcuts(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.getShortcuts(currentUserEmail));
    }
}
