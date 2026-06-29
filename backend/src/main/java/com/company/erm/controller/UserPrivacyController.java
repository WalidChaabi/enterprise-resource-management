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

@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
public class UserPrivacyController {

    private final UserPrivacyService userPrivacyService;

    public UserPrivacyController(UserPrivacyService userPrivacyService) {
        this.userPrivacyService = userPrivacyService;
    }

    @PostMapping("/data-export")
    public ResponseEntity<byte[]> exportPersonalData(Authentication authentication) {
        return userPrivacyService.exportPersonalData(authentication.getName());
    }

    @PostMapping("/erasure-request")
    public ResponseEntity<ErasureRequestResponseDTO> requestErasure(Authentication authentication) {
        return ResponseEntity.ok(userPrivacyService.requestErasure(authentication.getName()));
    }

    @GetMapping("/erasure-request")
    public ResponseEntity<ErasureRequestResponseDTO> getErasureRequest(Authentication authentication) {
        ErasureRequestResponseDTO response = userPrivacyService.getErasureRequest(authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
