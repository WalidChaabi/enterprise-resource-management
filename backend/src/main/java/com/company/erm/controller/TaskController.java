package com.company.erm.controller;

import com.company.erm.dto.TaskCreateDTO;
import com.company.erm.dto.TaskUpdateDTO;
import com.company.erm.dto.TaskResponseDTO;
import com.company.erm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for task management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects/{projectId}/tasks")
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'EMPLOYEE')")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create a new task for a project.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param projectId Project ID
     * @param createDTO Task creation data
     * @param authentication Current authenticated user
     * @return Created task with 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskResponseDTO> createTask(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TaskResponseDTO created = taskService.createTask(
            organizationId,
            projectId,
            createDTO,
            currentUserEmail
        );
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Get task by ID.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param projectId Project ID (for URL consistency, but taskId is used)
     * @param taskId Task ID
     * @param authentication Current authenticated user
     * @return Task details
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTask(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TaskResponseDTO task = taskService.getTask(
            organizationId,
            taskId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(task);
    }

    /**
     * Update a task.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param projectId Project ID (for URL consistency)
     * @param taskId Task ID
     * @param updateDTO Update data
     * @param authentication Current authenticated user
     * @return Updated task
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TaskResponseDTO updated = taskService.updateTask(
            organizationId,
            taskId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List tasks for a project.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param projectId Project ID
     * @param authentication Current authenticated user
     * @return List of tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> listTasks(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        List<TaskResponseDTO> tasks = taskService.listTasksByProject(
            organizationId,
            projectId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(tasks);
    }

    /**
     * Delete a task.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER only
     * 
     * @param organizationId Organization ID
     * @param projectId Project ID (for URL consistency)
     * @param taskId Task ID
     * @param authentication Current authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        taskService.deleteTask(
            organizationId,
            taskId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}

