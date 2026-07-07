package com.company.erm.controller;

import com.company.erm.dto.LeaveRequestCreateDTO;
import com.company.erm.dto.LeaveRequestUpdateDTO;
import com.company.erm.dto.LeaveRequestResponseDTO;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import com.company.erm.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.net.URI;
import java.util.UUID;

/**
 * REST controller for leave request management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Leave Requests", description = "Leave request workflow")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/leave-requests")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN', 'DEPARTMENT_MANAGER', 'EMPLOYEE')")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    /**
     * Create a new leave request.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param createDTO Leave request creation data
     * @param authentication Current authenticated user
     * @return Created leave request with 201 status
     */
    @Operation(summary = "Create Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LeaveRequestResponseDTO> createLeaveRequest(
            @PathVariable UUID organizationId,
            @Valid @RequestBody LeaveRequestCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO created = leaveRequestService.createLeaveRequest(
            organizationId,
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
     * Get leave request by ID.
     * 
     * Permissions: 
     * - Employees: their own requests
     * - Managers: any request in organization
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param authentication Current authenticated user
     * @return Leave request details
     */
    @Operation(summary = "Get Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{leaveRequestId}")
    public ResponseEntity<LeaveRequestResponseDTO> getLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO leaveRequest = leaveRequestService.getLeaveRequest(
            organizationId,
            leaveRequestId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(leaveRequest);
    }

    /**
     * Update a leave request.
     * 
     * Permissions: Employees can update their own pending requests
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param updateDTO Update data
     * @param authentication Current authenticated user
     * @return Updated leave request
     */
    @Operation(summary = "Update Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/{leaveRequestId}")
    public ResponseEntity<LeaveRequestResponseDTO> updateLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            @Valid @RequestBody LeaveRequestUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO updated = leaveRequestService.updateLeaveRequest(
            organizationId,
            leaveRequestId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List leave requests with optional filters.
     * 
     * Permissions:
     * - Employees: their own requests
     * - Managers: all requests in organization/department
     * 
     * @param organizationId Organization ID
     * @param employeeId Optional employee filter
     * @param departmentId Optional department filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param status Optional status filter
     * @param leaveType Optional leave type filter
     * @param authentication Current authenticated user
     * @return List of leave requests
     */
    @Operation(summary = "List Leave Requests")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<LeaveRequestResponseDTO>> listLeaveRequests(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) LeaveType leaveType,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<LeaveRequestResponseDTO> requests = leaveRequestService.listLeaveRequests(
            organizationId,
            employeeId,
            departmentId,
            startDate,
            endDate,
            status,
            leaveType,
            pageable,
            currentUserEmail
        );
        
        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a leave request.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER only
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param authentication Current authenticated user
     * @return Approved leave request
     */
    @Operation(summary = "Approve Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/{leaveRequestId}/approve")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN', 'DEPARTMENT_MANAGER')")
    public ResponseEntity<LeaveRequestResponseDTO> approveLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO updated = leaveRequestService.approveLeaveRequest(
            organizationId,
            leaveRequestId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Reject a leave request.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER only
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param rejectionReason Reason for rejection
     * @param authentication Current authenticated user
     * @return Rejected leave request
     */
    @Operation(summary = "Reject Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/{leaveRequestId}/reject")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN', 'DEPARTMENT_MANAGER')")
    public ResponseEntity<LeaveRequestResponseDTO> rejectLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO updated = leaveRequestService.rejectLeaveRequest(
            organizationId,
            leaveRequestId,
            rejectionReason,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Cancel a leave request.
     * 
     * Permissions: Employees can cancel their own requests
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param authentication Current authenticated user
     * @return Cancelled leave request
     */
    @Operation(summary = "Cancel Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/{leaveRequestId}/cancel")
    public ResponseEntity<LeaveRequestResponseDTO> cancelLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        LeaveRequestResponseDTO updated = leaveRequestService.cancelLeaveRequest(
            organizationId,
            leaveRequestId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a leave request.
     * 
     * Permissions: Employees can delete their own pending/rejected requests
     * 
     * @param organizationId Organization ID
     * @param leaveRequestId Leave request ID
     * @param authentication Current authenticated user
     * @return 204 No Content
     */
    @Operation(summary = "Delete Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{leaveRequestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteLeaveRequest(
            @PathVariable UUID organizationId,
            @PathVariable UUID leaveRequestId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        leaveRequestService.deleteLeaveRequest(
            organizationId,
            leaveRequestId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}

