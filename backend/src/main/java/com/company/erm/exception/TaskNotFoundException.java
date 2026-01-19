package com.company.erm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception thrown when a task is not found.
 */
public class TaskNotFoundException extends BusinessException {
    
    public TaskNotFoundException(UUID taskId) {
        super(
            "TASK_NOT_FOUND",
            "Task not found with id: " + taskId,
            HttpStatus.NOT_FOUND
        );
    }
}

