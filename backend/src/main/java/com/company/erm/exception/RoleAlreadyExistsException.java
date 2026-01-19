package com.company.erm.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a role with the same code already exists in the organization.
 */
public class RoleAlreadyExistsException extends BusinessException {
    
    public RoleAlreadyExistsException(String code) {
        super(
            "ROLE_ALREADY_EXISTS",
            "Role with code '" + code + "' already exists in organization",
            HttpStatus.CONFLICT
        );
    }
}

