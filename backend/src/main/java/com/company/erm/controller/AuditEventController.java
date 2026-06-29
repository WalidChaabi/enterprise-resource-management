package com.company.erm.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.AuditEventResponseDTO;
import com.company.erm.service.AuditEventService;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/audit-events")
@PreAuthorize("hasRole('ORG_ADMIN')")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditEventResponseDTO>> listAuditEvents(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(auditEventService.listEvents(
                organizationId, authentication.getName(), action, entityType, pageable));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditEvents(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            Authentication authentication) {
        return auditEventService.exportCsv(organizationId, authentication.getName(), action, entityType);
    }
}
