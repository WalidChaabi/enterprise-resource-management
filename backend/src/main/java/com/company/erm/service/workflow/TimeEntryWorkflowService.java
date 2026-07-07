package com.company.erm.service.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.TimeEntryResponseDTO;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.TimeEntryNotFoundException;
import com.company.erm.mapper.TimeEntryMapper;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.service.NotificationService;
import com.company.erm.service.TimeEntryDomainService;
import com.company.erm.service.WebhookDispatchService;
import com.company.erm.webhook.WebhookEventType;

@Service
@Transactional
public class TimeEntryWorkflowService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final TimeEntryDomainService timeEntryDomainService;
    private final PermissionChecker permissionChecker;
    private final TimeEntryMapper timeEntryMapper;
    private final NotificationService notificationService;
    private final WebhookDispatchService webhookDispatchService;

    public TimeEntryWorkflowService(
            TimeEntryRepository timeEntryRepository,
            UserRepository userRepository,
            TimeEntryDomainService timeEntryDomainService,
            PermissionChecker permissionChecker,
            TimeEntryMapper timeEntryMapper,
            NotificationService notificationService,
            WebhookDispatchService webhookDispatchService) {
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.timeEntryDomainService = timeEntryDomainService;
        this.permissionChecker = permissionChecker;
        this.timeEntryMapper = timeEntryMapper;
        this.notificationService = notificationService;
        this.webhookDispatchService = webhookDispatchService;
    }

    public TimeEntryResponseDTO submitTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        TimeEntry timeEntry = loadForOrganization(timeEntryId, organizationId);
        User currentUser = requireUser(currentUserEmail);

        if (!timeEntry.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Only the employee who created the entry can submit it");
        }

        timeEntryDomainService.validateStatusTransition(timeEntry.getStatus(), TimeEntryStatus.SUBMITTED);
        timeEntry.setStatus(TimeEntryStatus.SUBMITTED);

        TimeEntry updated = timeEntryRepository.save(timeEntry);
        dispatchSubmittedWebhook(organizationId, updated);
        return timeEntryMapper.toResponseDTO(updated);
    }

    public TimeEntryResponseDTO approveTimeEntry(UUID organizationId, UUID timeEntryId, String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        TimeEntry timeEntry = loadForOrganization(timeEntryId, organizationId);
        timeEntryDomainService.validateStatusTransition(timeEntry.getStatus(), TimeEntryStatus.APPROVED);

        User approver = requireUser(currentUserEmail);
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

    public TimeEntryResponseDTO rejectTimeEntry(
            UUID organizationId,
            UUID timeEntryId,
            String rejectionReason,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "PROJECT_MANAGER", "HR_ADMIN");

        TimeEntry timeEntry = loadForOrganization(timeEntryId, organizationId);
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

    private TimeEntry loadForOrganization(UUID timeEntryId, UUID organizationId) {
        return timeEntryRepository.findByIdAndEmployeeOrganizationId(timeEntryId, organizationId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private void dispatchSubmittedWebhook(UUID organizationId, TimeEntry timeEntry) {
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
}
