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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for leave request management use cases.
 * 
 * Responsibilities:
 * - Orchestrate use cases
 * - Coordinate between domain and infrastructure
 * - Handle transactions
 * - Enforce security
 */
@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveRequestDomainService leaveRequestDomainService;
    private final PermissionChecker permissionChecker;
    private final LeaveRequestMapper leaveRequestMapper;
    private final NotificationService notificationService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveRequestDomainService leaveRequestDomainService,
            PermissionChecker permissionChecker,
            LeaveRequestMapper leaveRequestMapper,
            NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveRequestDomainService = leaveRequestDomainService;
        this.permissionChecker = permissionChecker;
        this.leaveRequestMapper = leaveRequestMapper;
        this.notificationService = notificationService;
    }

    /**
     * Create a new leave request.
     * 
     * Permissions: Employees can create their own requests
     */
    @Transactional
    public LeaveRequestResponseDTO createLeaveRequest(
            UUID organizationId,
            LeaveRequestCreateDTO createDTO,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER", "EMPLOYEE");
        
        // 2. Load current user (employee)
        User employee = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!employee.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("User does not belong to organization");
        }
        
        // 3. Create leave request entity
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(createDTO.getLeaveType());
        leaveRequest.setStartDate(createDTO.getStartDate());
        leaveRequest.setEndDate(createDTO.getEndDate());
        leaveRequest.setReason(createDTO.getReason());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        
        // 4. Business validation
        leaveRequestDomainService.validateLeaveRequestCreation(leaveRequest);
        
        // 5. Check for overlapping approved requests
        List<LeaveRequest> existingRequests = leaveRequestRepository
            .findOverlappingApprovedRequests(employee.getId(), createDTO.getStartDate(), createDTO.getEndDate());
        leaveRequestDomainService.validateNoOverlap(leaveRequest, existingRequests);
        
        // 6. Save
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        
        // 7. Map to DTO
        return leaveRequestMapper.toResponseDTO(saved);
    }

    /**
     * Get leave request by ID.
     */
    @Transactional(readOnly = true)
    public LeaveRequestResponseDTO getLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // Security: employees can only see their own requests, managers can see all
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");
        
        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only view your own leave requests");
        }
        
        return leaveRequestMapper.toResponseDTO(leaveRequest);
    }

    /**
     * Update a leave request.
     * 
     * Permissions: Employees can update their own pending requests
     */
    @Transactional
    public LeaveRequestResponseDTO updateLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            LeaveRequestUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // 1. Load leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // 2. Security check - employees can only update their own requests
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN");
        
        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only update your own leave requests");
        }
        
        // 3. Apply updates
        updateDTO.getLeaveType().ifPresent(leaveRequest::setLeaveType);
        updateDTO.getStartDate().ifPresent(leaveRequest::setStartDate);
        updateDTO.getEndDate().ifPresent(leaveRequest::setEndDate);
        updateDTO.getReason().ifPresent(leaveRequest::setReason);
        
        // 4. Business validation
        leaveRequestDomainService.validateLeaveRequestUpdate(leaveRequest);
        
        // 5. Check for overlaps if dates changed
        if (updateDTO.getStartDate().isPresent() || updateDTO.getEndDate().isPresent()) {
            List<LeaveRequest> existingRequests = leaveRequestRepository
                .findOverlappingApprovedRequests(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getStartDate(),
                    leaveRequest.getEndDate()
                );
            leaveRequestDomainService.validateNoOverlap(leaveRequest, existingRequests);
        }
        
        // 6. Save
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        
        // 7. Map to DTO
        return leaveRequestMapper.toResponseDTO(updated);
    }

    /**
     * List leave requests with filters.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDTO> listLeaveRequests(
            UUID organizationId,
            UUID employeeId,
            UUID departmentId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveRequestStatus status,
            com.company.erm.entity.LeaveType leaveType,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER", "EMPLOYEE");
        
        // 2. Load current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");
        
        // 3. If not manager, can only see own requests
        if (!isManager) {
            employeeId = currentUser.getId();
        }
        
        // 4. Build query based on filters
        List<LeaveRequest> requests;
        
        if (employeeId != null && startDate != null && endDate != null) {
            requests = leaveRequestRepository.findByEmployeeIdAndStartDateBetween(employeeId, startDate, endDate);
        } else if (employeeId != null && status != null) {
            requests = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
        } else if (employeeId != null) {
            requests = leaveRequestRepository.findByEmployeeId(employeeId);
        } else if (departmentId != null) {
            requests = leaveRequestRepository.findByDepartmentId(departmentId);
        } else {
            // List all requests for organization (managers only)
            if (!isManager) {
                throw new BusinessException("Access denied: Only managers can view all leave requests");
            }
            requests = leaveRequestRepository.findByOrganizationId(organizationId);
        }
        
        // 5. Apply additional filters
        if (status != null) {
            requests = requests.stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
        }
        
        if (leaveType != null) {
            requests = requests.stream()
                .filter(r -> r.getLeaveType() == leaveType)
                .collect(Collectors.toList());
        }
        
        // 6. Filter by organization
        requests = requests.stream()
            .filter(r -> r.getEmployee().getOrganization().getId().equals(organizationId))
            .collect(Collectors.toList());
        
        // 7. Map to DTOs
        return requests.stream()
            .map(leaveRequestMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Approve a leave request.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER
     */
    @Transactional
    public LeaveRequestResponseDTO approveLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");
        
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // Department managers can only approve requests from their department
        boolean isDeptManager = permissionChecker.hasRole(organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        boolean isHrAdmin = permissionChecker.hasRole(organizationId, currentUserEmail, "HR_ADMIN", "ORG_ADMIN");
        
        if (isDeptManager && !isHrAdmin) {
            User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
            
            if (leaveRequest.getEmployee().getDepartment() == null ||
                !leaveRequest.getEmployee().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new BusinessException("Department managers can only approve leave requests from their own department");
            }
        }
        
        leaveRequestDomainService.validateStatusTransition(leaveRequest.getStatus(), LeaveRequestStatus.APPROVED);
        
        User approver = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(java.time.LocalDateTime.now());
        leaveRequest.setRejectionReason(null);
        
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        notificationService.createInAppNotification(
            updated.getEmployee(),
            "Leave request approved",
            String.format("Your %s leave from %s to %s was approved.",
                updated.getLeaveType(), updated.getStartDate(), updated.getEndDate()));
        return leaveRequestMapper.toResponseDTO(updated);
    }

    /**
     * Reject a leave request.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER
     */
    @Transactional
    public LeaveRequestResponseDTO rejectLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String rejectionReason,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN", "DEPARTMENT_MANAGER");
        
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // Department managers can only reject requests from their department
        boolean isDeptManager = permissionChecker.hasRole(organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        boolean isHrAdmin = permissionChecker.hasRole(organizationId, currentUserEmail, "HR_ADMIN", "ORG_ADMIN");
        
        if (isDeptManager && !isHrAdmin) {
            User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
            
            if (leaveRequest.getEmployee().getDepartment() == null ||
                !leaveRequest.getEmployee().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new BusinessException("Department managers can only reject leave requests from their own department");
            }
        }
        
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
        notificationService.createInAppNotification(updated.getEmployee(), "Leave request rejected", description);
        return leaveRequestMapper.toResponseDTO(updated);
    }

    /**
     * Cancel a leave request.
     * 
     * Permissions: Employees can cancel their own requests
     */
    @Transactional
    public LeaveRequestResponseDTO cancelLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // Only the employee who created it can cancel
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Only the employee who created the request can cancel it");
        }
        
        leaveRequestDomainService.validateStatusTransition(leaveRequest.getStatus(), LeaveRequestStatus.CANCELLED);
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return leaveRequestMapper.toResponseDTO(updated);
    }

    /**
     * Delete a leave request.
     * 
     * Permissions: Employees can delete their own pending/rejected requests
     */
    @Transactional
    public void deleteLeaveRequest(
            UUID organizationId,
            UUID leaveRequestId,
            String currentUserEmail) {
        
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndEmployeeOrganizationId(leaveRequestId, organizationId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        
        // Security: employees can only delete their own requests
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        boolean isManager = permissionChecker.hasRole(organizationId, currentUserEmail, 
            "ORG_ADMIN", "HR_ADMIN");
        
        if (!isManager && !leaveRequest.getEmployee().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied: You can only delete your own leave requests");
        }
        
        // Business validation
        leaveRequestDomainService.validateLeaveRequestDeletion(leaveRequest);
        
        // Delete
        leaveRequestRepository.delete(leaveRequest);
    }
}

