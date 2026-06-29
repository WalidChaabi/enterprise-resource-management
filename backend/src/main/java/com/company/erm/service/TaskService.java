package com.company.erm.service;

import com.company.erm.dto.TaskCreateDTO;
import com.company.erm.dto.TaskUpdateDTO;
import com.company.erm.dto.TaskResponseDTO;
import com.company.erm.entity.Project;
import com.company.erm.entity.Task;
import com.company.erm.entity.TaskStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.TaskNotFoundException;
import com.company.erm.mapper.TaskMapper;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for task management use cases.
 * 
 * Responsibilities:
 * - Orchestrate use cases
 * - Coordinate between domain and infrastructure
 * - Handle transactions
 * - Enforce security
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskDomainService taskDomainService;
    private final PermissionChecker permissionChecker;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskDomainService taskDomainService,
            PermissionChecker permissionChecker,
            TaskMapper taskMapper,
            NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskDomainService = taskDomainService;
        this.permissionChecker = permissionChecker;
        this.taskMapper = taskMapper;
        this.notificationService = notificationService;
    }

    /**
     * Create a new task for a project.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, or project members
     */
    @Transactional
    public TaskResponseDTO createTask(
            UUID organizationId,
            UUID projectId,
            TaskCreateDTO createDTO,
            String currentUserEmail) {
        
        // 1. Security check - user must have access to the organization
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "EMPLOYEE");
        
        // 2. Load project and validate it belongs to organization
        Project project = projectRepository.findByIdAndOrganizationId(projectId, organizationId)
            .orElseThrow(() -> new BusinessException("Project not found or does not belong to organization"));
        
        if (project.isArchived()) {
            throw new BusinessException("Cannot create task for archived project");
        }
        
        // 3. Create task entity
        Task task = new Task();
        task.setProject(project);
        task.setTitle(createDTO.getTitle());
        task.setDescription(createDTO.getDescription());
        task.setPriority(createDTO.getPriority());
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(createDTO.getDueDate());
        
        // 4. Assign to user if provided
        if (createDTO.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(createDTO.getAssignedToId())
                .orElseThrow(() -> new BusinessException("Assigned user not found"));
            
            // Validate user belongs to same organization
            if (!assignedTo.getOrganization().getId().equals(organizationId)) {
                throw new BusinessException("Assigned user must belong to the same organization");
            }
            
            task.setAssignedTo(assignedTo);
        }
        
        // 5. Business validation
        taskDomainService.validateTaskCreation(task);
        
        // 6. Save task
        Task savedTask = taskRepository.save(task);

        if (savedTask.getAssignedTo() != null) {
            notificationService.notifyUser(
                savedTask.getAssignedTo(),
                "Task assigned",
                String.format("You were assigned to task \"%s\" on project %s.",
                    savedTask.getTitle(), project.getName()));
        }
        
        // 7. Map to DTO
        return taskMapper.toResponseDTO(savedTask);
    }

    /**
     * Get task by ID.
     */
    @Transactional(readOnly = true)
    public TaskResponseDTO getTask(
            UUID organizationId,
            UUID taskId,
            String currentUserEmail) {
        
        Task task = taskRepository.findByIdAndProjectOrganizationId(taskId, organizationId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        return taskMapper.toResponseDTO(task);
    }

    /**
     * Update a task.
     */
    @Transactional
    public TaskResponseDTO updateTask(
            UUID organizationId,
            UUID taskId,
            TaskUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // 1. Load task
        Task task = taskRepository.findByIdAndProjectOrganizationId(taskId, organizationId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        // 2. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "EMPLOYEE");
        
        // 3. Track old status for validation
        TaskStatus oldStatus = task.getStatus();
        User previousAssignee = task.getAssignedTo();
        
        // 4. Apply updates
        updateDTO.getTitle().ifPresent(task::setTitle);
        updateDTO.getDescription().ifPresent(task::setDescription);
        updateDTO.getPriority().ifPresent(task::setPriority);
        updateDTO.getDueDate().ifPresent(task::setDueDate);
        
        // 5. Handle status change with validation
        if (updateDTO.getStatus().isPresent()) {
            TaskStatus newStatus = updateDTO.getStatus().get();
            taskDomainService.validateStatusTransition(oldStatus, newStatus);
            task.setStatus(newStatus);
        }
        
        // 6. Handle assignment change
        if (updateDTO.getAssignedToId().isPresent()) {
            UUID assignedToId = updateDTO.getAssignedToId().get();
            if (assignedToId != null) {
                User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new BusinessException("Assigned user not found"));
                
                if (!assignedTo.getOrganization().getId().equals(organizationId)) {
                    throw new BusinessException("Assigned user must belong to the same organization");
                }
                
                task.setAssignedTo(assignedTo);
            } else {
                task.setAssignedTo(null);
            }
        }
        
        // 7. Business validation
        taskDomainService.validateTaskUpdate(task);
        
        // 8. Save
        Task updated = taskRepository.save(task);

        User newAssignee = updated.getAssignedTo();
        if (newAssignee != null
                && (previousAssignee == null || !newAssignee.getId().equals(previousAssignee.getId()))) {
            notificationService.notifyUser(
                newAssignee,
                "Task assigned",
                String.format("You were assigned to task \"%s\" on project %s.",
                    updated.getTitle(), updated.getProject().getName()));
        }
        
        // 9. Map to DTO
        return taskMapper.toResponseDTO(updated);
    }

    /**
     * List tasks for a project.
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> listTasksByProject(
            UUID organizationId,
            UUID projectId,
            String currentUserEmail) {
        
        // 1. Verify project belongs to organization (validation only)
        projectRepository.findByIdAndOrganizationId(projectId, organizationId)
            .orElseThrow(() -> new BusinessException("Project not found or does not belong to organization"));
        
        // 2. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "EMPLOYEE");
        
        // 3. Load tasks
        List<Task> tasks = taskRepository.findByProjectIdAndOrganizationId(projectId, organizationId);
        
        // 4. Map to DTOs
        return tasks.stream()
            .map(taskMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Delete a task.
     */
    @Transactional
    public void deleteTask(
            UUID organizationId,
            UUID taskId,
            String currentUserEmail) {
        
        // 1. Load task
        Task task = taskRepository.findByIdAndProjectOrganizationId(taskId, organizationId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        // 2. Security check - only ORG_ADMIN and PROJECT_MANAGER can delete
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER");
        
        // 3. Business validation
        taskDomainService.validateTaskDeletion(task);
        
        // 4. Delete
        taskRepository.delete(task);
    }
}

