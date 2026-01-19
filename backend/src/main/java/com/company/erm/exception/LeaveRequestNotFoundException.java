package com.company.erm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception thrown when a leave request is not found.
 */
public class LeaveRequestNotFoundException extends BusinessException {
    
    public LeaveRequestNotFoundException(UUID leaveRequestId) {
        super(
            "LEAVE_REQUEST_NOT_FOUND",
            "Leave request not found with id: " + leaveRequestId,
            HttpStatus.NOT_FOUND
        );
    }
}

