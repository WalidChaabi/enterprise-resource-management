package com.company.erm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.webhook.WebhookDeliveryResponseDTO;
import com.company.erm.dto.webhook.WebhookEndpointCreatedResponseDTO;
import com.company.erm.dto.webhook.WebhookEndpointRequestDTO;
import com.company.erm.dto.webhook.WebhookEndpointResponseDTO;
import com.company.erm.service.WebhookEndpointService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Webhooks", description = "Outbound webhook configuration")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/webhooks")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
public class WebhookController {

    private final WebhookEndpointService webhookEndpointService;

    public WebhookController(WebhookEndpointService webhookEndpointService) {
        this.webhookEndpointService = webhookEndpointService;
    }

    @Operation(summary = "List Endpoints")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<List<WebhookEndpointResponseDTO>> listEndpoints(
            @PathVariable UUID organizationId,
            Authentication authentication) {
        return ResponseEntity.ok(webhookEndpointService.listEndpoints(organizationId, authentication.getName()));
    }

    @Operation(summary = "Create Endpoint")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    public ResponseEntity<WebhookEndpointCreatedResponseDTO> createEndpoint(
            @PathVariable UUID organizationId,
            @Valid @RequestBody WebhookEndpointRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(webhookEndpointService.createEndpoint(organizationId, request, authentication.getName()));
    }

    @Operation(summary = "Update Endpoint")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/{endpointId}")
    public ResponseEntity<WebhookEndpointResponseDTO> updateEndpoint(
            @PathVariable UUID organizationId,
            @PathVariable UUID endpointId,
            @Valid @RequestBody WebhookEndpointRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(
                webhookEndpointService.updateEndpoint(organizationId, endpointId, request, authentication.getName()));
    }

    @Operation(summary = "Delete Endpoint")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{endpointId}")
    public ResponseEntity<Void> deleteEndpoint(
            @PathVariable UUID organizationId,
            @PathVariable UUID endpointId,
            Authentication authentication) {
        webhookEndpointService.deleteEndpoint(organizationId, endpointId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List Deliveries")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/deliveries")
    public ResponseEntity<Page<WebhookDeliveryResponseDTO>> listDeliveries(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID endpointId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(
                webhookEndpointService.listDeliveries(organizationId, endpointId, authentication.getName(), pageable));
    }
}
