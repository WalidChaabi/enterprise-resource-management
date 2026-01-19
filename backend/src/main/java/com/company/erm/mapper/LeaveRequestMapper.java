package com.company.erm.mapper;

import com.company.erm.dto.LeaveRequestResponseDTO;
import com.company.erm.entity.LeaveRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between LeaveRequest entities and LeaveRequest DTOs.
 */
@Component
public class LeaveRequestMapper {

    /**
     * Convert LeaveRequest entity to LeaveRequestResponseDTO.
     */
    public LeaveRequestResponseDTO toResponseDTO(LeaveRequest leaveRequest) {
        LeaveRequestResponseDTO dto = new LeaveRequestResponseDTO();
        dto.setId(leaveRequest.getId());
        
        // Employee information
        if (leaveRequest.getEmployee() != null) {
            dto.setEmployeeId(leaveRequest.getEmployee().getId());
            dto.setEmployeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
            dto.setEmployeeEmail(leaveRequest.getEmployee().getEmail());
            
            // Department information
            if (leaveRequest.getEmployee().getDepartment() != null) {
                dto.setDepartmentId(leaveRequest.getEmployee().getDepartment().getId());
                dto.setDepartmentName(leaveRequest.getEmployee().getDepartment().getName());
            }
        }
        
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setTotalDays(leaveRequest.getTotalDays());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        
        // Approval information
        if (leaveRequest.getApprovedBy() != null) {
            dto.setApprovedById(leaveRequest.getApprovedBy().getId());
            dto.setApprovedByName(leaveRequest.getApprovedBy().getFirstName() + " " + leaveRequest.getApprovedBy().getLastName());
        }
        
        dto.setApprovedAt(leaveRequest.getApprovedAt());
        dto.setRejectionReason(leaveRequest.getRejectionReason());
        dto.setCreatedAt(leaveRequest.getCreatedAt());
        dto.setUpdatedAt(leaveRequest.getUpdatedAt());
        
        return dto;
    }
}

