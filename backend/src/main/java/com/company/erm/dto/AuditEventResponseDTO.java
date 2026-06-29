package com.company.erm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class AuditEventResponseDTO {
    private UUID id;
    private UUID organizationId;
    private String actorEmail;
    private UUID actorId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String payloadJson;
    private LocalDateTime createdAt;
}
