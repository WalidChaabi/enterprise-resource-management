package com.company.erm.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a department with the same code already exists in the organization.
 */
public class DepartmentAlreadyExistsException extends BusinessException {
    
    public DepartmentAlreadyExistsException(String code) {
        super(
            "DEPARTMENT_ALREADY_EXISTS",
            "Department with code '" + code + "' already exists in organization",
            HttpStatus.CONFLICT
        );
    }
}

