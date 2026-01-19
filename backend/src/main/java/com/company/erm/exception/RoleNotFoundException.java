package com.company.erm.exception;

import org.springframework.http.HttpStatus;
import java.util.UUID;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends BusinessException {
    
    public RoleNotFoundException(UUID roleId) {
        super(
            "ROLE_NOT_FOUND",
            "Role with ID " + roleId + " not found",
            HttpStatus.NOT_FOUND
        );
    }
    
    public RoleNotFoundException(String code) {
        super(
            "ROLE_NOT_FOUND",
            "Role with code '" + code + "' not found",
            HttpStatus.NOT_FOUND
        );
    }
}

