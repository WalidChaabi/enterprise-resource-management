package com.company.erm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for role response.
 */
public record RoleResponseDTO(
    UUID id,
    UUID organizationId,
    String organizationName,
    String code,
    String name,
    String description,
    String scope,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

