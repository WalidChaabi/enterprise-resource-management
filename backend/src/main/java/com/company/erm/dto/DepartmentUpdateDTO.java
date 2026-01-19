package com.company.erm.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Optional;
import java.util.UUID;

/**
 * DTO for updating an existing department.
 * All fields are optional - only provided fields will be updated.
 */
public record DepartmentUpdateDTO(
    
    @Size(max = 255, message = "Name must not exceed 255 characters")
    Optional<String> name,
    
    @Size(max = 100, message = "Code must not exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must contain only uppercase letters, numbers, and underscores")
    Optional<String> code,
    
    Optional<UUID> parentDepartmentId,
    
    Optional<Boolean> active
) {
    public DepartmentUpdateDTO {
        // Ensure at least one field is provided
        if (name.isEmpty() && code.isEmpty() && parentDepartmentId.isEmpty() && active.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }
}
