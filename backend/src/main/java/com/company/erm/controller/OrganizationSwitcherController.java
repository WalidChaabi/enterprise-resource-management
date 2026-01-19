package com.company.erm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.OrganizationSummaryDTO;
import com.company.erm.dto.OrganizationSwitchRequestDTO;
import com.company.erm.dto.OrganizationSwitchResponseDTO;
import com.company.erm.service.OrganizationSwitcherService;

import jakarta.validation.Valid;

/**
 * REST controller for organization switching.
 */
@RestController
@RequestMapping("/api/organizations")
@PreAuthorize("isAuthenticated()")
public class OrganizationSwitcherController {

    private final OrganizationSwitcherService organizationSwitcherService;

    public OrganizationSwitcherController(OrganizationSwitcherService organizationSwitcherService) {
        this.organizationSwitcherService = organizationSwitcherService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationSummaryDTO>> listOrganizations(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        List<OrganizationSummaryDTO> orgs = organizationSwitcherService
            .listOrganizationsForUser(currentUserEmail);
        return ResponseEntity.ok(orgs);
    }

    @PostMapping("/switch")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrganizationSwitchResponseDTO> switchOrganization(
            @Valid @RequestBody OrganizationSwitchRequestDTO request,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        OrganizationSwitchResponseDTO response = organizationSwitcherService
            .switchOrganization(currentUserEmail, request.getOrgId());
        return ResponseEntity.ok(response);
    }
}
