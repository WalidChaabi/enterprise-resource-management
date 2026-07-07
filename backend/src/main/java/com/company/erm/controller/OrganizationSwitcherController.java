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
import com.company.erm.security.AuthCookieService;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.OrganizationSwitcherService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization Switcher", description = "Switch active organization context")
@RestController
@RequestMapping("/api/organizations")
@PreAuthorize("isAuthenticated()")
public class OrganizationSwitcherController {

    private final OrganizationSwitcherService organizationSwitcherService;
    private final AuthCookieService authCookieService;

    public OrganizationSwitcherController(
            OrganizationSwitcherService organizationSwitcherService,
            AuthCookieService authCookieService) {
        this.organizationSwitcherService = organizationSwitcherService;
        this.authCookieService = authCookieService;
    }

    @Operation(summary = "List Organizations")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<List<OrganizationSummaryDTO>> listOrganizations(Authentication authentication) {
        List<OrganizationSummaryDTO> orgs = organizationSwitcherService
            .listOrganizationsForUser(authentication.getName());
        return ResponseEntity.ok(orgs);
    }

    @Operation(summary = "Switch Organization")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/switch")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrganizationSwitchResponseDTO> switchOrganization(
            @Valid @RequestBody OrganizationSwitchRequestDTO request,
            Authentication authentication,
            HttpServletResponse response) {
        AuthSession session = organizationSwitcherService.switchOrganization(
                authentication.getName(), request.getOrgId());
        authCookieService.writeAccessCookie(response, session.accessToken());

        OrganizationSwitchResponseDTO body = new OrganizationSwitchResponseDTO(
                request.getOrgId(),
                session.response().accessToken(),
                "Bearer");
        return ResponseEntity.ok(body);
    }
}
