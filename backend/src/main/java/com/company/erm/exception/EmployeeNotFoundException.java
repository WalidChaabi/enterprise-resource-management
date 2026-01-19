package com.company.erm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends BusinessException {
    
    public EmployeeNotFoundException(UUID employeeId) {
        super(
            "EMPLOYEE_NOT_FOUND",
            "Employee not found with id: " + employeeId,
            HttpStatus.NOT_FOUND
        );
    }
}

