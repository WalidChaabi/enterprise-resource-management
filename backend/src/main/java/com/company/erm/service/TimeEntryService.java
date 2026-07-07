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
import com.company.erm.service.workflow.TimeEntryWorkflowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    private final TimeEntryWorkflowService timeEntryWorkflowService;

    public TimeEntryService(
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TimeEntryDomainService timeEntryDomainService,
            PermissionChecker permissionChecker,
            TimeEntryMapper timeEntryMapper,
            TimeEntryWorkflowService timeEntryWorkflowService) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.timeEntryDomainService = timeEntryDomainService;
        this.permissionChecker = permissionChecker;
        this.timeEntryMapper = timeEntryMapper;
        this.timeEntryWorkflowService = timeEntryWorkflowService;
    }

    @Transactional
    public TimeEntryResponseDTO createTimeEntry(
            UUID organizationId,
            TimeEntryCreateDTO createDTO,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "EMPLOYEE");

        User employee = requireOrgUser(currentUserEmail, organizationId);
        Project project = projectRepository.findByIdAndOrganizationId(createDTO.getProjectId(), organizationId)
                .orElseThrow(() -> new BusinessException("Project not found or does not belong to organization"));

        if (project.isArchived()) {
            throw new BusinessException("Cannot create time entry for archived project");
        }

        Task task = resolveTask(createDTO.getTaskId(), project);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setProject(project);
        timeEntry.setTask(task);
        timeEntry.setEntryDate(createDTO.getEntryDate());
        timeEntry.setStartTime(createDTO.getStartTime());
        timeEntry.setEndTime(createDTO.getEndTime());
        timeEntry.setDescription(createDTO.getDescription());
        timeEntry.setStatus(TimeEntryStatus.DRAFT);
        timeEntry.calculateDuration();

        timeEntryDomainService.validateTimeEntryCreation(timeEntry);
        List<TimeEntry> existingEntries = timeEntryRepository
                .findByEmployeeIdAndEntryDate(employee.getId(), createDTO.getEntryDate());
        timeEntryDomainService.validateNoOverlap(timeEntry, existingEntries);

        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(timeEntry));
    }

    @Transactional(readOnly = true)
    public TimeEntryResponseDTO getTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only view your own time entries");
        }

        return timeEntryMapper.toResponseDTO(timeEntry);
    }

    @Transactional
    public TimeEntryResponseDTO updateTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            TimeEntryUpdateDTO updateDTO,
            String currentUserEmail) {
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only update your own time entries");
        }

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

        if (updateDTO.getStartTime().isPresent() || updateDTO.getEndTime().isPresent()) {
            timeEntry.calculateDuration();
        }

        timeEntryDomainService.validateTimeEntryUpdate(timeEntry);

        if (updateDTO.getEntryDate().isPresent()
                || updateDTO.getStartTime().isPresent()
                || updateDTO.getEndTime().isPresent()) {
            List<TimeEntry> existingEntries = timeEntryRepository
                    .findByEmployeeIdAndEntryDate(timeEntry.getEmployee().getId(), timeEntry.getEntryDate());
            timeEntryDomainService.validateNoOverlap(timeEntry, existingEntries);
        }

        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(timeEntry));
    }

    @Transactional(readOnly = true)
    public Page<TimeEntryResponseDTO> listTimeEntries(
            UUID organizationId,
            UUID employeeId,
            UUID projectId,
            UUID taskId,
            LocalDate startDate,
            LocalDate endDate,
            TimeEntryStatus status,
            Pageable pageable,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN", "EMPLOYEE");

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        if (!isManager) {
            employeeId = currentUser.getId();
        }

        return timeEntryRepository.findByOrganizationFilters(
                organizationId, employeeId, projectId, taskId, status, startDate, endDate, pageable)
                .map(timeEntryMapper::toResponseDTO);
    }

    public TimeEntryResponseDTO submitTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        return timeEntryWorkflowService.submitTimeEntry(organizationId, timeEntryId, currentUserEmail);
    }

    public TimeEntryResponseDTO approveTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        return timeEntryWorkflowService.approveTimeEntry(organizationId, timeEntryId, currentUserEmail);
    }

    public TimeEntryResponseDTO rejectTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String rejectionReason,
            String currentUserEmail) {
        return timeEntryWorkflowService.rejectTimeEntry(organizationId, timeEntryId, rejectionReason, currentUserEmail);
    }

    @Transactional
    public void deleteTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        TimeEntry timeEntry = timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        if (!isManager && !timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only delete your own time entries");
        }

        timeEntryDomainService.validateTimeEntryDeletion(timeEntry);
        timeEntryRepository.delete(timeEntry);
    }

    private User requireOrgUser(String email, UUID organizationId) {
        User user = requireUser(email);
        if (!user.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("User does not belong to organization");
        }
        return user;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private Task resolveTask(UUID taskId, Project project) {
        if (taskId == null) {
            return null;
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found"));
        if (!task.getProject().getId().equals(project.getId())) {
            throw new BusinessException("Task does not belong to the specified project");
        }
        return task;
    }
}
