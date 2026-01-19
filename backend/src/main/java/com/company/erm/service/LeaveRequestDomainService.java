package com.company.erm.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.exception.BusinessException;

@Service
public class LeaveRequestDomainService {

    /**
     * Validate leave request creation business rules.
     */
    public void validateLeaveRequestCreation(LeaveRequest leaveRequest) {
        // 1. End date must be after or equal to start date
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new BusinessException("End date must be after or equal to start date");
        }

        // 2. Start date cannot be too far in the past (e.g., more than 30 days)
        LocalDate maxPastDate = LocalDate.now().minusDays(30);
        if (leaveRequest.getStartDate().isBefore(maxPastDate)) {
            throw new BusinessException("Start date cannot be more than 30 days in the past");
        }

        // 3. Total days must be positive
        if (leaveRequest.getTotalDays() == null || leaveRequest.getTotalDays() <= 0) {
            throw new BusinessException("Total days must be greater than zero");
        }

        // 4. Maximum leave duration (e.g., 90 days)
        if (leaveRequest.getTotalDays() > 90) {
            throw new BusinessException("Leave request cannot exceed 90 days");
        }
    }

    /**
     * Validate leave request update business rules.
     */
    public void validateLeaveRequestUpdate(LeaveRequest leaveRequest) {
        // Cannot update approved or cancelled requests
        if (leaveRequest.getStatus() == LeaveRequestStatus.APPROVED) {
            throw new BusinessException("Cannot update approved leave request");
        }

        if (leaveRequest.getStatus() == LeaveRequestStatus.CANCELLED) {
            throw new BusinessException("Cannot update cancelled leave request");
        }

        // Re-validate dates
        validateLeaveRequestCreation(leaveRequest);
    }

    /**
     * Validate status transition.
     */
    public void validateStatusTransition(LeaveRequestStatus current, LeaveRequestStatus next) {
        // PENDING -> APPROVED, REJECTED, CANCELLED
        // APPROVED -> CANCELLED (only by employee)
        // REJECTED -> PENDING (resubmit)
        // CANCELLED -> (cannot change)

        if (current == LeaveRequestStatus.CANCELLED && next != LeaveRequestStatus.CANCELLED) {
            throw new BusinessException("Cannot change status of cancelled leave request");
        }

        if (current == LeaveRequestStatus.APPROVED && next == LeaveRequestStatus.PENDING) {
            throw new BusinessException("Cannot resubmit approved leave request");
        }

        if (current == LeaveRequestStatus.REJECTED && next == LeaveRequestStatus.APPROVED) {
            throw new BusinessException("Cannot approve rejected request. Must resubmit first.");
        }
    }

    /**
     * Check if leave request can be deleted.
     */
    public void validateLeaveRequestDeletion(LeaveRequest leaveRequest) {
        if (leaveRequest.getStatus() == LeaveRequestStatus.APPROVED) {
            throw new BusinessException("Cannot delete approved leave request. Cancel it instead.");
        }
    }

    /**
     * Check for overlapping approved leave requests.
     */
    public void validateNoOverlap(LeaveRequest leaveRequest, java.util.List<LeaveRequest> existingRequests) {
        for (LeaveRequest existing : existingRequests) {
            if (existing.getId().equals(leaveRequest.getId())) {
                continue; // Skip self
            }

            // Only check approved requests for overlap
            if (existing.getStatus() != LeaveRequestStatus.APPROVED) {
                continue;
            }

            // Check for date overlap
            boolean overlaps = !(leaveRequest.getEndDate().isBefore(existing.getStartDate()) ||
                                leaveRequest.getStartDate().isAfter(existing.getEndDate()));

            if (overlaps) {
                throw new BusinessException(
                    String.format("Leave request overlaps with approved leave from %s to %s",
                        existing.getStartDate(),
                        existing.getEndDate())
                );
            }
        }
    }

    /**
     * Calculate remaining leave days for an employee.
     * This is a placeholder - in production, this would query approved leave requests
     * and calculate based on leave type and organization policies.
     */
    public int calculateRemainingDays(UUID employeeId, com.company.erm.entity.LeaveType leaveType) {
        // TODO: Implement calculation based on:
        // - Employee's leave balance
        // - Approved leave requests for the year
        // - Organization leave policies
        // For now, return a default value
        return 25; // Default annual leave days
    }
}

