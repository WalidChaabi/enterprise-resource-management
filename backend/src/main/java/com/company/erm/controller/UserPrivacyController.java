package com.company.erm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.privacy.ErasureRequestResponseDTO;
import com.company.erm.service.UserPrivacyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Privacy", description = "Personal data export and erasure")
@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
public class UserPrivacyController {

    private final UserPrivacyService userPrivacyService;

    public UserPrivacyController(UserPrivacyService userPrivacyService) {
        this.userPrivacyService = userPrivacyService;
    }

    @Operation(summary = "Export Personal Data")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/data-export")
    public ResponseEntity<byte[]> exportPersonalData(Authentication authentication) {
        return userPrivacyService.exportPersonalData(authentication.getName());
    }

    @Operation(summary = "Request Erasure")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/erasure-request")
    public ResponseEntity<ErasureRequestResponseDTO> requestErasure(Authentication authentication) {
        return ResponseEntity.ok(userPrivacyService.requestErasure(authentication.getName()));
    }

    @Operation(summary = "Get Erasure Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/erasure-request")
    public ResponseEntity<ErasureRequestResponseDTO> getErasureRequest(Authentication authentication) {
        ErasureRequestResponseDTO response = userPrivacyService.getErasureRequest(authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
