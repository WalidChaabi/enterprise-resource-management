package com.company.erm.dto;

import jakarta.validation.constraints.*;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO for updating an existing employee.
 * All fields are optional - only provided fields will be updated.
 */
public record EmployeeUpdateDTO(
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    Optional<String> email,
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    Optional<String> firstName,
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
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

