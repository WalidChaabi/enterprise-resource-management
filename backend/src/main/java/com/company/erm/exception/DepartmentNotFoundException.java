package com.company.erm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception thrown when a department is not found.
 */
public class DepartmentNotFoundException extends BusinessException {
    
    public DepartmentNotFoundException(UUID departmentId) {
        super(
            "DEPARTMENT_NOT_FOUND",
            "Department not found with id: " + departmentId,
            HttpStatus.NOT_FOUND
        );
    }
}

