package com.company.erm.service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.AuditEventResponseDTO;
import com.company.erm.entity.AuditEvent;
import com.company.erm.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.AuditEventRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.security.RoleCode;

@Service
@Transactional(readOnly = true)
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final PermissionChecker permissionChecker;
    private final ObjectMapper objectMapper;

    public AuditEventService(
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            PermissionChecker permissionChecker,
            ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.permissionChecker = permissionChecker;
        this.objectMapper = objectMapper;
    }

    public Page<AuditEventResponseDTO> listEvents(
            UUID organizationId,
            String currentUserEmail,
            String action,
            String entityType,
            Pageable pageable) {
        requireOrgAdmin(organizationId, currentUserEmail);
        return auditEventRepository
                .findByOrganizationWithFilters(organizationId, blankToNull(action), blankToNull(entityType), pageable)
                .map(this::toDto);
    }

    public ResponseEntity<byte[]> exportCsv(
            UUID organizationId,
            String currentUserEmail,
            String action,
            String entityType) {
        requireOrgAdmin(organizationId, currentUserEmail);
        List<AuditEvent> events = auditEventRepository
                .findByOrganizationWithFilters(
                        organizationId, blankToNull(action), blankToNull(entityType), Pageable.ofSize(10_000))
                .getContent();

        StringWriter writer = new StringWriter();
        writer.append("created_at,action,entity_type,entity_id,actor_email,payload_json\n");
        for (AuditEvent event : events) {
            writer.append(csv(event.getCreatedAt().toString()));
            writer.append(',');
            writer.append(csv(event.getAction()));
            writer.append(',');
            writer.append(csv(event.getEntityType()));
            writer.append(',');
            writer.append(csv(event.getEntityId() != null ? event.getEntityId().toString() : ""));
            writer.append(',');
            writer.append(csv(event.getActorEmail()));
            writer.append(',');
            writer.append(csv(event.getPayloadJson()));
            writer.append('\n');
        }

        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-events.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(AuditLogService.AuditEvent event) {
        AuditEvent entity = new AuditEvent();
        entity.setOrganizationId(event.organizationId());
        entity.setActorEmail(event.actor());
        entity.setAction(event.eventType());
        entity.setEntityType(event.entityType());
        entity.setEntityId(event.entityId());
        entity.setCreatedAt(event.timestamp());
        userRepository.findByEmail(event.actor()).ifPresent(user -> entity.setActorId(user.getId()));
        try {
            if (event.details() != null && !event.details().isEmpty()) {
                entity.setPayloadJson(objectMapper.writeValueAsString(event.details()));
            }
        } catch (IOException ex) {
            entity.setPayloadJson(null);
        }
        auditEventRepository.save(entity);
    }

    private void requireOrgAdmin(UUID organizationId, String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail, RoleCode.ORG_ADMIN);
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        if (!user.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("ORG_MISMATCH", "Organization mismatch", HttpStatus.FORBIDDEN);
        }
    }

    private AuditEventResponseDTO toDto(AuditEvent event) {
        AuditEventResponseDTO dto = new AuditEventResponseDTO();
        dto.setId(event.getId());
        dto.setOrganizationId(event.getOrganizationId());
        dto.setActorEmail(event.getActorEmail());
        dto.setActorId(event.getActorId());
        dto.setAction(event.getAction());
        dto.setEntityType(event.getEntityType());
        dto.setEntityId(event.getEntityId());
        dto.setPayloadJson(event.getPayloadJson());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
