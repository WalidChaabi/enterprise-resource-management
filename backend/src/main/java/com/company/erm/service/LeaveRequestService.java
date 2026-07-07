package com.company.erm.service;

import com.company.erm.dto.LeaveRequestCreateDTO;
import com.company.erm.dto.LeaveRequestUpdateDTO;
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
import com.company.erm.service.workflow.LeaveRequestWorkflowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveRequestDomainService leaveRequestDomainService;
    private final PermissionChecker permissionChecker;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveRequestWorkflowService leaveRequestWorkflowService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveRequestDomainService leaveRequestDomainService,
            PermissionChecker permissionChecker,
            LeaveRequestMapper leaveRequestMapper,
            LeaveRequestWorkflowService leaveRequestWorkflowService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveRequestDomainService = leaveRequestDomainService;
        this.permissionChecker = permissionChecker;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leaveRequestWorkflowService = leaveRequestWorkflowService;
    }

    @Transactional
    public LeaveRequestResponseDTO createLeaveRequest(
            UUID organizationId,
            LeaveRequestCreateDTO createDTO,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER", "EMPLOYEE");

        User employee = requireOrgUser(currentUserEmail, organizationId);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(createDTO.getLeaveType());
        leaveRequest.setStartDate(createDTO.getStartDate());
        leaveRequest.setEndDate(createDTO.getEndDate());
        leaveRequest.setReason(createDTO.getReason());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        leaveRequestDomainService.validateLeaveRequestCreation(leaveRequest);
        List<LeaveRequest> existingRequests = leaveRequestRepository
                .findOverlappingApprovedRequests(employee.getId(), createDTO.getStartDate(), createDTO.getEndDate());
        leaveRequestDomainService.validateNoOverlap(leaveRequest, existingRequests);

        return leaveRequestMapper.toResponseDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponseDTO getLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");

        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only view your own leave requests");
        }

        return leaveRequestMapper.toResponseDTO(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponseDTO updateLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            LeaveRequestUpdateDTO updateDTO,
            String currentUserEmail) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, "ORG_ADMIN", "HR_ADMIN");

        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only update your own leave requests");
        }

        updateDTO.getLeaveType().ifPresent(leaveRequest::setLeaveType);
        updateDTO.getStartDate().ifPresent(leaveRequest::setStartDate);
        updateDTO.getEndDate().ifPresent(leaveRequest::setEndDate);
        updateDTO.getReason().ifPresent(leaveRequest::setReason);

        leaveRequestDomainService.validateLeaveRequestUpdate(leaveRequest);

        if (updateDTO.getStartDate().isPresent() || updateDTO.getEndDate().isPresent()) {
            List<LeaveRequest> existingRequests = leaveRequestRepository.findOverlappingApprovedRequests(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getStartDate(),
                    leaveRequest.getEndDate());
            leaveRequestDomainService.validateNoOverlap(leaveRequest, existingRequests);
        }

        return leaveRequestMapper.toResponseDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestResponseDTO> listLeaveRequests(
            UUID organizationId,
            UUID employeeId,
            UUID departmentId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveRequestStatus status,
            com.company.erm.entity.LeaveType leaveType,
            Pageable pageable,
            String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER", "EMPLOYEE");

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail,
                "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");

        if (!isManager) {
            employeeId = currentUser.getId();
        }

        return leaveRequestRepository.findByOrganizationFilters(
                organizationId, employeeId, departmentId, status, leaveType, startDate, endDate, pageable)
                .map(leaveRequestMapper::toResponseDTO);
    }

    public LeaveRequestResponseDTO approveLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        return leaveRequestWorkflowService.approveLeaveRequest(organizationId, leaveRequestId, currentUserEmail);
    }

    public LeaveRequestResponseDTO rejectLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String rejectionReason,
            String currentUserEmail) {
        return leaveRequestWorkflowService.rejectLeaveRequest(
                organizationId, leaveRequestId, rejectionReason, currentUserEmail);
    }

    @Transactional
    public LeaveRequestResponseDTO cancelLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        User currentUser = requireUser(currentUserEmail);
        if (!leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Only the employee who created the request can cancel it");
        }

        leaveRequestDomainService.validateStatusTransition(leaveRequest.getStatus(), LeaveRequestStatus.CANCELLED);
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);

        return leaveRequestMapper.toResponseDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public void deleteLeaveRequest(UUID organizationId, UUID leaveRequestId, String currentUserEmail) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        User currentUser = requireUser(currentUserEmail);
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, "ORG_ADMIN", "HR_ADMIN");

        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only delete your own leave requests");
        }

        leaveRequestDomainService.validateLeaveRequestDeletion(leaveRequest);
        leaveRequestRepository.delete(leaveRequest);
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
}
