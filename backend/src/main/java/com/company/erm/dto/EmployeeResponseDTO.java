package com.company.erm.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for employee response.
 * Contains all employee information including relationships.
 */
public record EmployeeResponseDTO(
    UUID id,
    UUID organizationId,
    String organizationName,
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String email,
    String firstName,
    String lastName,
    boolean active,
    List<RoleDTO> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String lastModifiedBy
) {
    public record RoleDTO(
        UUID id,
        String code,
        String name,
        String scope
    ) {}
}

