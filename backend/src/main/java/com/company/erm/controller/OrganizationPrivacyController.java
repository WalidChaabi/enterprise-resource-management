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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization Privacy", description = "Org-level privacy and data export")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/privacy")
@PreAuthorize("isAuthenticated()")
public class OrganizationPrivacyController {

    private final UserPrivacyService userPrivacyService;

    public OrganizationPrivacyController(UserPrivacyService userPrivacyService) {
        this.userPrivacyService = userPrivacyService;
    }

    @Operation(summary = "List Pending Erasure Requests")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/erasure-requests")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
    public ResponseEntity<List<ErasureRequestResponseDTO>> listPendingErasureRequests(
            @PathVariable UUID organizationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                userPrivacyService.listPendingErasureRequests(organizationId, authentication.getName()));
    }

    @Operation(summary = "Approve Erasure Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/erasure-requests/{requestId}/approve")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
    public ResponseEntity<ErasureRequestResponseDTO> approveErasureRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ErasureProcessRequestDTO body,
            Authentication authentication) {
        String notes = body != null ? body.getAdminNotes() : null;
        return ResponseEntity.ok(userPrivacyService.approveErasureRequest(
                organizationId, requestId, authentication.getName(), notes));
    }

    @Operation(summary = "Reject Erasure Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/erasure-requests/{requestId}/reject")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
    public ResponseEntity<ErasureRequestResponseDTO> rejectErasureRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ErasureProcessRequestDTO body,
            Authentication authentication) {
        String notes = body != null ? body.getAdminNotes() : null;
        return ResponseEntity.ok(userPrivacyService.rejectErasureRequest(
                organizationId, requestId, authentication.getName(), notes));
    }

    @Operation(summary = "Process Employee Erasure")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/users/{userId}/erasure")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN')")
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
