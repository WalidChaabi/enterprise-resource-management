package com.company.erm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for department response.
 * Contains all department information including relationships.
 */
public record DepartmentResponseDTO(
    UUID id,
    UUID organizationId,
    String organizationName,
    String name,
    String code,
    UUID parentDepartmentId,
    String parentDepartmentName,
    String parentDepartmentCode,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String lastModifiedBy
) {}

