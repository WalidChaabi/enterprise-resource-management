package com.company.erm.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lacks sufficient permissions for an operation.
 */
public class InsufficientPermissionException extends BusinessException {
    
    public InsufficientPermissionException(String message) {
        super(
            "INSUFFICIENT_PERMISSION",
            message,
            HttpStatus.FORBIDDEN
        );
    }
}

