package com.company.erm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception thrown when a time entry is not found.
 */
public class TimeEntryNotFoundException extends BusinessException {
    
    public TimeEntryNotFoundException(UUID timeEntryId) {
        super(
            "TIME_ENTRY_NOT_FOUND",
            "Time entry not found with id: " + timeEntryId,
            HttpStatus.NOT_FOUND
        );
    }
}

