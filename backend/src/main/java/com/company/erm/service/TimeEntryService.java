package com.company.erm.service;

import com.company.erm.dto.TimeEntryCreateDTO;
import com.company.erm.dto.TimeEntryUpdateDTO;
import com.company.erm.dto.TimeEntryResponseDTO;
import com.company.erm.entity.Project;
import com.company.erm.entity.Task;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.TimeEntryNotFoundException;
import com.company.erm.mapper.TimeEntryMapper;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.webhook.WebhookEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for time entry management use cases.
 * 
 * Responsibilities:
 * - Orchestrate use cases
 * - Coordinate between domain and infrastructure
 * - Handle transactions
 * - Enforce security
 */
@Service
@Transactional
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TimeEntryDomainService timeEntryDomainService;
    private final PermissionChecker permissionChecker;
    private final TimeEntryMapper timeEntryMapper;
    private final NotificationService notificationService;
    private final WebhookDispatchService webhookDispatchService;

    public TimeEntryService(
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TimeEntryDomainService timeEntryDomainService,
            PermissionChecker permissionChecker,
            TimeEntryMapper timeEntryMapper,
            NotificationService notificationService,
            WebhookDispatchService webhookDispatchService) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.timeEntryDomainService = timeEntryDomainService;
        this.permissionChecker = permissionChecker;
        this.timeEntryMapper = timeEntryMapper;
        this.notificationService = notificationService;
        this.webhookDispatchService = webhookDispatchService;
    }

    /**
     * Create a new time entry.
     * 
     * Permissions: Employees can create their own entries
     */
    @Transactional
    public TimeEntryResponseDTO createTimeEntry(
            UUID organizationId,
            TimeEntryCreateDTO createDTO,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "EMPLOYEE");
        
        // 2. Load current user (employee)
        User employee = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!employee.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("User does not belong to organization");
        }
        
        // 3. Load project and validate it belongs to organization
        Project project = projectRepository.findByIdAndOrganizationId(createDTO.getProjectId(), organizationId)
            .orElseThrow(() -> new BusinessException("Project not found or does not belong to organization"));
        
        if (project.isArchived()) {
            throw new BusinessException("Cannot create time entry for archived project");
        }
        
        // 4. Load task if provided
        Task task = null;
        if (createDTO.getTaskId() != null) {
            task = taskRepository.findById(createDTO.getTaskId())
                .orElseThrow(() -> new BusinessException("Task not found"));
            
            // Validate task belongs to the project
            if (!task.getProject().getId().equals(project.getId())) {
                throw new BusinessException("Task does not belong to the specified project");
            }
        }
        
        // 5. Create time entry entity
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setProject(project);
        timeEntry.setTask(task);
        timeEntry.setEntryDate(createDTO.getEntryDate());
        timeEntry.setStartTime(createDTO.getStartTime());
        timeEntry.setEndTime(createDTO.getEndTime());
        timeEntry.setDescription(createDTO.getDescription());
        timeEntry.setStatus(TimeEntryStatus.DRAFT);
        
        // Calculate duration before validation (since validation checks duration)
        timeEntry.calculateDuration();
        
        // 6. Business validation
        timeEntryDomainService.validateTimeEntryCreation(timeEntry);
        
        // 7. Check for overlapping entries on the same day
        List<TimeEntry> existingEntries = timeEntryRepository
            .findByEmployeeIdAndEntryDate(employee.getId(), createDTO.getEntryDate());
        timeEntryDomainService.validateNoOverlap(timeEntry, existingEntries);
        
        // 8. Save
        TimeEntry saved = timeEntryRepository.save(timeEntry);
        
        // 9. Map to DTO
        return timeEntryMapper.toResponseDTO(saved);
    }

    /**
     * Get time entry by ID.
     */
    @Transactional(readOnly = true)
    public TimeEntryResponseDTO getTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String currentUserEmail) {
        
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        // Security: employees can only see their own entries, managers can see all
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only view your own time entries");
        }
        
        return timeEntryMapper.toResponseDTO(timeEntry);
    }

    /**
     * Update a time entry.
     * 
     * Permissions: Employees can update their own draft/submitted entries
     */
    @Transactional
    public TimeEntryResponseDTO updateTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            TimeEntryUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // 1. Load time entry
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        // 2. Security check - employees can only update their own entries
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only update your own time entries");
        }
        
        // 3. Apply updates
        updateDTO.getTaskId().ifPresent(taskId -> {
            if (taskId != null) {
                Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new BusinessException("Task not found"));
                if (!task.getProject().getId().equals(timeEntry.getProject().getId())) {
                    throw new BusinessException("Task does not belong to the project");
                }
                timeEntry.setTask(task);
            } else {
                timeEntry.setTask(null);
            }
        });
        
        updateDTO.getEntryDate().ifPresent(timeEntry::setEntryDate);
        updateDTO.getStartTime().ifPresent(timeEntry::setStartTime);
        updateDTO.getEndTime().ifPresent(timeEntry::setEndTime);
        updateDTO.getDescription().ifPresent(timeEntry::setDescription);
        
        // Recalculate duration if start/end times changed
        if (updateDTO.getStartTime().isPresent() || updateDTO.getEndTime().isPresent()) {
            timeEntry.calculateDuration();
        }
        
        // 5. Business validation
        timeEntryDomainService.validateTimeEntryUpdate(timeEntry);
        
        // 6. Check for overlaps if date/time changed
        if (updateDTO.getEntryDate().isPresent() || updateDTO.getStartTime().isPresent() || updateDTO.getEndTime().isPresent()) {
            List<TimeEntry> existingEntries = timeEntryRepository
                .findByEmployeeIdAndEntryDate(timeEntry.getEmployee().getId(), timeEntry.getEntryDate());
            timeEntryDomainService.validateNoOverlap(timeEntry, existingEntries);
        }
        
        // 7. Save
        TimeEntry updated = timeEntryRepository.save(timeEntry);
        
        // 8. Map to DTO
        return timeEntryMapper.toResponseDTO(updated);
    }

    /**
     * List time entries with filters.
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponseDTO> listTimeEntries(
            UUID organizationId,
            UUID employeeId,
            UUID projectId,
            UUID taskId,
            LocalDate startDate,
            LocalDate endDate,
            TimeEntryStatus status,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN", "EMPLOYEE");
        
        // 2. Load current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        // 3. If not manager, can only see own entries
        if (!isManager) {
            employeeId = currentUser.getId();
        }
        
        // 4. Build query based on filters
        List<TimeEntry> entries;
        
        if (employeeId != null && startDate != null && endDate != null) {
            entries = timeEntryRepository.findByEmployeeIdAndEntryDateBetween(employeeId, startDate, endDate);
        } else if (projectId != null && startDate != null && endDate != null) {
            entries = timeEntryRepository.findByProjectIdAndEntryDateBetween(projectId, startDate, endDate);
        } else if (employeeId != null && status != null) {
            entries = timeEntryRepository.findByEmployeeIdAndStatus(employeeId, status);
        } else if (employeeId != null) {
            entries = timeEntryRepository.findByEmployeeId(employeeId);
        } else if (projectId != null) {
            entries = timeEntryRepository.findByProjectId(projectId);
        } else if (taskId != null) {
            entries = timeEntryRepository.findByTaskId(taskId);
        } else {
            // List all entries for organization (managers only)
            if (!isManager) {
                throw new BusinessException("Access denied: Only managers can view all time entries");
            }
            entries = timeEntryRepository.findByOrganizationId(organizationId);
        }
        
        // 5. Apply additional filters
        if (status != null) {
            entries = entries.stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
        }
        
        if (taskId != null) {
            entries = entries.stream()
                .filter(e -> e.getTask() != null && e.getTask().getId().equals(taskId))
                .collect(Collectors.toList());
        }
        
        // 6. Filter by organization
        entries = entries.stream()
            .filter(e -> e.getEmployee().getOrganization().getId().equals(organizationId))
            .collect(Collectors.toList());
        
        // 7. Map to DTOs
        return entries.stream()
            .map(timeEntryMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Submit time entry for approval.
     */
    @Transactional
    public TimeEntryResponseDTO submitTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String currentUserEmail) {
        
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        // Only the employee who created it can submit
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Only the employee who created the entry can submit it");
        }
        
        timeEntryDomainService.validateStatusTransition(timeEntry.getStatus(), TimeEntryStatus.SUBMITTED);
        timeEntry.setStatus(TimeEntryStatus.SUBMITTED);
        
        TimeEntry updated = timeEntryRepository.save(timeEntry);
        dispatchTimeEntrySubmittedWebhook(organizationId, updated);
        return timeEntryMapper.toResponseDTO(updated);
    }

    private void dispatchTimeEntrySubmittedWebhook(UUID organizationId, TimeEntry timeEntry) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timeEntryId", timeEntry.getId().toString());
        data.put("employeeId", timeEntry.getEmployee().getId().toString());
        data.put("employeeEmail", timeEntry.getEmployee().getEmail());
        data.put("projectId", timeEntry.getProject().getId().toString());
        if (timeEntry.getTask() != null) {
            data.put("taskId", timeEntry.getTask().getId().toString());
        }
        data.put("entryDate", timeEntry.getEntryDate().toString());
        data.put("durationHours", timeEntry.getDurationHours());
        data.put("status", timeEntry.getStatus().name());
        webhookDispatchService.dispatch(organizationId, WebhookEventType.TIMEENTRY_SUBMITTED, data);
    }

    /**
     * Approve a time entry.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN
     */
    @Transactional
    public TimeEntryResponseDTO approveTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        timeEntryDomainService.validateStatusTransition(timeEntry.getStatus(), TimeEntryStatus.APPROVED);
        
        User approver = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        timeEntry.setStatus(TimeEntryStatus.APPROVED);
        timeEntry.setApprovedBy(approver);
        timeEntry.setApprovedAt(java.time.LocalDateTime.now());
        timeEntry.setRejectionReason(null);
        
        TimeEntry updated = timeEntryRepository.save(timeEntry);
        notificationService.notifyUser(
            updated.getEmployee(),
            "Time entry approved",
            String.format("Your time entry for %s on %s was approved.",
                updated.getProject().getName(), updated.getEntryDate()));
        return timeEntryMapper.toResponseDTO(updated);
    }

    /**
     * Reject a time entry.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN
     */
    @Transactional
    public TimeEntryResponseDTO rejectTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String rejectionReason,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        timeEntryDomainService.validateStatusTransition(timeEntry.getStatus(), TimeEntryStatus.REJECTED);
        
        timeEntry.setStatus(TimeEntryStatus.REJECTED);
        timeEntry.setRejectionReason(rejectionReason);
        timeEntry.setApprovedBy(null);
        timeEntry.setApprovedAt(null);
        
        TimeEntry updated = timeEntryRepository.save(timeEntry);
        String description = rejectionReason != null && !rejectionReason.isBlank()
            ? String.format("Your time entry for %s on %s was rejected: %s",
                updated.getProject().getName(), updated.getEntryDate(), rejectionReason)
            : String.format("Your time entry for %s on %s was rejected.",
                updated.getProject().getName(), updated.getEntryDate());
        notificationService.notifyUser(updated.getEmployee(), "Time entry rejected", description);
        return timeEntryMapper.toResponseDTO(updated);
    }

    /**
     * Delete a time entry.
     * 
     * Permissions: Employees can delete their own draft entries
     */
    @Transactional
    public void deleteTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String currentUserEmail) {
        
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
            .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        // Security: employees can only delete their own entries
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");
        
        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only delete your own time entries");
        }
        
        // Business validation
        timeEntryDomainService.validateTimeEntryDeletion(timeEntry);
        
        // Delete
        timeEntryRepository.delete(timeEntry);
    }
}

