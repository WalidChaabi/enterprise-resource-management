package com.company.erm.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for creating a new employee.
 * All fields are required for employee creation.
 */
public record EmployeeCreateDTO(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase, lowercase, digit, and special character"
    )
    String password,
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,
    
    @NotNull(message = "Department ID is required")
    UUID departmentId,
    
    @NotNull(message = "Default role ID is required")
    UUID defaultRoleId
) {}

