package com.company.erm.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/**
 * DTO for updating an existing role.
 * All fields are optional - only provided fields will be updated.
 */
public record RoleUpdateDTO(
    
    @Size(max = 150, message = "Name must not exceed 150 characters")
    Optional<String> name,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    Optional<String> description,
    
    @Pattern(regexp = "^(GLOBAL|ORG)$", message = "Scope must be either GLOBAL or ORG")
    Optional<String> scope
) {
    public RoleUpdateDTO {
        // Ensure at least one field is provided
        if (name.isEmpty() && description.isEmpty() && scope.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }
}

