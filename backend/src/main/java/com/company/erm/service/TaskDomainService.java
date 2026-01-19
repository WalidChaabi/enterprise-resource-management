package com.company.erm.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.company.erm.entity.Task;
import com.company.erm.entity.TaskStatus;
import com.company.erm.exception.BusinessException;

@Service
public class TaskDomainService {

    /**
     * Validate task creation business rules.
     */
    public void validateTaskCreation(Task task) {
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Due date cannot be in the past");
        }
    }

    /**
     * Validate task update business rules.
     */
    public void validateTaskUpdate(Task task) {
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) 
            && task.getStatus() != TaskStatus.DONE) {
            throw new BusinessException("Due date cannot be in the past for active tasks");
        }
    }

    /**
     * Validate status transition.
     */
    public void validateStatusTransition(TaskStatus current, TaskStatus next) {
        if (current == TaskStatus.DONE && next != TaskStatus.DONE) {
            throw new BusinessException("Cannot change status from DONE to another status");
        }
    }

    /**
     * Check if task can be deleted.
     */
    public void validateTaskDeletion(Task task) {
        // Add business rules for task deletion if needed
        // For example: cannot delete if task is in progress and has dependencies
    }
}

