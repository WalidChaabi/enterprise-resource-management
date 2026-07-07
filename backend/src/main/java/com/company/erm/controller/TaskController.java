package com.company.erm.controller;

import com.company.erm.dto.TaskCreateDTO;
import com.company.erm.dto.TaskUpdateDTO;
import com.company.erm.dto.TaskResponseDTO;
import com.company.erm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for task management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tasks", description = "Project task management")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects/{projectId}/tasks")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'EMPLOYEE')")
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
    @Operation(summary = "Create Task")
    @ApiResponse(responseCode = "200", description = "Success")
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
    @Operation(summary = "Get Task")
    @ApiResponse(responseCode = "200", description = "Success")
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
    @Operation(summary = "Update Task")
    @ApiResponse(responseCode = "200", description = "Success")
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
    @Operation(summary = "List Tasks")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> listTasks(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<TaskResponseDTO> tasks = taskService.listTasksByProject(
            organizationId,
            projectId,
            pageable,
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
    @Operation(summary = "Delete Task")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{taskId}")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER')")
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

