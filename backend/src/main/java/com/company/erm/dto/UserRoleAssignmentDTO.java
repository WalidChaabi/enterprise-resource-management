package com.company.erm.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for assigning a role to a user.
 */
public record UserRoleAssignmentDTO(
    
    @NotNull(message = "Role ID is required")
    UUID roleId
) {}

