package com.company.erm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.config.UiConfigProperties;
import com.company.erm.dto.UiConfigResponseDTO;

/**
 * REST controller for UI configuration.
 */
@RestController
@RequestMapping("/api/config")
public class UiConfigController {

    private final UiConfigProperties uiConfigProperties;

    public UiConfigController(UiConfigProperties uiConfigProperties) {
        this.uiConfigProperties = uiConfigProperties;
    }

    @GetMapping("/ui")
    public ResponseEntity<UiConfigResponseDTO> getUiConfig() {
        UiConfigResponseDTO dto = new UiConfigResponseDTO();
        dto.setDocumentationUrl(uiConfigProperties.getDocumentationUrl());
        dto.setSupportUrl(uiConfigProperties.getSupportUrl());
        dto.setShortcutsEnabled(uiConfigProperties.isShortcutsEnabled());
        return ResponseEntity.ok(dto);
    }
}
