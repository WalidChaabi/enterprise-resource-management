package com.company.erm.service.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.LeaveRequestResponseDTO;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.LeaveRequestNotFoundException;
import com.company.erm.mapper.LeaveRequestMapper;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.service.LeaveRequestDomainService;
import com.company.erm.service.NotificationService;
import com.company.erm.service.WebhookDispatchService;
import com.company.erm.webhook.WebhookEventType;

@Service
@Transactional
public class LeaveRequestWorkflowService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveRequestDomainService leaveRequestDomainService;
    private final PermissionChecker permissionChecker;
    private final LeaveRequestMapper leaveRequestMapper;
    private final NotificationService notificationService;
    private final WebhookDispatchService webhookDispatchService;

    public LeaveRequestWorkflowService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveRequestDomainService leaveRequestDomainService,
            PermissionChecker permissionChecker,
            LeaveRequestMapper leaveRequestMapper,
            NotificationService notificationService,
            WebhookDispatchService webhookDispatchService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveRequestDomainService = leaveRequestDomainService;
        this.permissionChecker = permissionChecker;
        this.leaveRequestMapper = leaveRequestMapper;
        this.notificationService = notificationService;
        this.webhookDispatchService = webhookDispatchService;
    }

    public LeaveRequestResponseDTO approveLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");

        LeaveRequest leaveRequest = loadForOrganization(leaveRequestId, organizationId);
        enforceDepartmentManagerScope(organizationId, currentUserEmail, leaveRequest);

        leaveRequestDomainService.validateStatusTransition(leaveRequest.getStatus(), LeaveRequestStatus.APPROVED);

        User approver = requireUser(currentUserEmail);
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(java.time.LocalDateTime.now());
        leaveRequest.setRejectionReason(null);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        notificationService.notifyUser(
                updated.getEmployee(),
                "Leave request approved",
                String.format("Your %s leave from %s to %s was approved.",
                        updated.getLeaveType(), updated.getStartDate(), updated.getEndDate()));
        dispatchApprovedWebhook(organizationId, updated, approver);
        return leaveRequestMapper.toResponseDTO(updated);
    }

    public LeaveRequestResponseDTO rejectLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String rejectionReason,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");

        LeaveRequest leaveRequest = loadForOrganization(leaveRequestId, organizationId);
        enforceDepartmentManagerScope(organizationId, currentUserEmail, leaveRequest);

        leaveRequestDomainService.validateStatusTransition(leaveRequest.getStatus(), LeaveRequestStatus.REJECTED);

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setRejectionReason(rejectionReason);
        leaveRequest.setApprovedBy(null);
        leaveRequest.setApprovedAt(null);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        String description = rejectionReason != null && !rejectionReason.isBlank()
                ? String.format("Your leave request from %s to %s was rejected: %s",
                        updated.getStartDate(), updated.getEndDate(), rejectionReason)
                : String.format("Your leave request from %s to %s was rejected.",
                        updated.getStartDate(), updated.getEndDate());
        notificationService.notifyUser(updated.getEmployee(), "Leave request rejected", description);
        return leaveRequestMapper.toResponseDTO(updated);
    }

    private void enforceDepartmentManagerScope(UUID organizationId, String currentUserEmail, LeaveRequest leaveRequest) {
        boolean isDeptManager = permissionChecker.hasRole(organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        boolean isHrAdmin = permissionChecker.hasRole(organizationId, currentUserEmail, "HR_ADMIN", "ORG_ADMIN");

        if (!isDeptManager || isHrAdmin) {
            return;
        }

        User currentUser = requireUser(currentUserEmail);
        if (leaveRequest.getEmployee().getDepartment() == null
                || currentUser.getDepartment() == null
                || !leaveRequest.getEmployee().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
            throw new BusinessException(
                    "Department managers can only act on leave requests from their own department");
        }
    }

    private LeaveRequest loadForOrganization(UUID leaveRequestId, UUID organizationId) {
        return leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private void dispatchApprovedWebhook(UUID organizationId, LeaveRequest leaveRequest, User approver) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("leaveRequestId", leaveRequest.getId().toString());
        data.put("employeeId", leaveRequest.getEmployee().getId().toString());
        data.put("employeeEmail", leaveRequest.getEmployee().getEmail());
        data.put("leaveType", leaveRequest.getLeaveType().name());
        data.put("startDate", leaveRequest.getStartDate().toString());
        data.put("endDate", leaveRequest.getEndDate().toString());
        data.put("totalDays", leaveRequest.getTotalDays());
        data.put("approvedById", approver.getId().toString());
        data.put("approvedAt", leaveRequest.getApprovedAt() != null ? leaveRequest.getApprovedAt().toString() : null);
        webhookDispatchService.dispatch(organizationId, WebhookEventType.LEAVE_APPROVED, data);
    }
}
