package com.company.erm.dto;

import java.util.Optional;
import java.util.UUID;

/**
 * DTO for updating an existing employee.
 * All fields are optional - only provided fields will be updated.
 */
public record EmployeeUpdateDTO(
    Optional<String> email,
    Optional<String> firstName,
    Optional<String> lastName,
    Optional<UUID> departmentId,
    Optional<Boolean> active
) {
    public EmployeeUpdateDTO {
        // Ensure at least one field is provided
        if (email.isEmpty() && firstName.isEmpty() && lastName.isEmpty() 
            && departmentId.isEmpty() && active.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }
}

