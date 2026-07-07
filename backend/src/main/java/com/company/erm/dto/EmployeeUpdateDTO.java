package com.company.erm.dto;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.validation.constraints.AssertTrue;

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
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmployeeUpdateDTO {
        if (email.isEmpty() && firstName.isEmpty() && lastName.isEmpty()
            && departmentId.isEmpty() && active.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }

    @AssertTrue(message = "Email must be valid")
    public boolean isEmailValid() {
        return email.isEmpty()
            || (email.get().length() <= 255 && EMAIL_PATTERN.matcher(email.get()).matches());
    }

    @AssertTrue(message = "First name must not be blank when provided")
    public boolean isFirstNamePresentAndValid() {
        return firstName.isEmpty()
            || (!firstName.get().isBlank() && firstName.get().length() <= 100);
    }

    @AssertTrue(message = "Last name must not be blank when provided")
    public boolean isLastNamePresentAndValid() {
        return lastName.isEmpty()
            || (!lastName.get().isBlank() && lastName.get().length() <= 100);
    }
}
