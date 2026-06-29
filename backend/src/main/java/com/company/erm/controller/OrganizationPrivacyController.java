package com.company.erm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.privacy.ErasureProcessRequestDTO;
import com.company.erm.dto.privacy.ErasureRequestResponseDTO;
import com.company.erm.service.UserPrivacyService;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/privacy")
@PreAuthorize("isAuthenticated()")
public class OrganizationPrivacyController {

    private final UserPrivacyService userPrivacyService;

    public OrganizationPrivacyController(UserPrivacyService userPrivacyService) {
        this.userPrivacyService = userPrivacyService;
    }

    @GetMapping("/erasure-requests")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<List<ErasureRequestResponseDTO>> listPendingErasureRequests(
            @PathVariable UUID organizationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                userPrivacyService.listPendingErasureRequests(organizationId, authentication.getName()));
    }

    @PostMapping("/erasure-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ErasureRequestResponseDTO> approveErasureRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ErasureProcessRequestDTO body,
            Authentication authentication) {
        String notes = body != null ? body.getAdminNotes() : null;
        return ResponseEntity.ok(userPrivacyService.approveErasureRequest(
                organizationId, requestId, authentication.getName(), notes));
    }

    @PostMapping("/erasure-requests/{requestId}/reject")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ErasureRequestResponseDTO> rejectErasureRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ErasureProcessRequestDTO body,
            Authentication authentication) {
        String notes = body != null ? body.getAdminNotes() : null;
        return ResponseEntity.ok(userPrivacyService.rejectErasureRequest(
                organizationId, requestId, authentication.getName(), notes));
    }

    @PostMapping("/users/{userId}/erasure")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ErasureRequestResponseDTO> processEmployeeErasure(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @RequestBody(required = false) ErasureProcessRequestDTO body,
            Authentication authentication) {
        String notes = body != null ? body.getAdminNotes() : null;
        return ResponseEntity.ok(userPrivacyService.processEmployeeErasure(
                organizationId, userId, authentication.getName(), notes));
    }
}
