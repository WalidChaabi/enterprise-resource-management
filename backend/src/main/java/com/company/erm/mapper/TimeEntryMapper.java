package com.company.erm.mapper;

import com.company.erm.dto.TimeEntryResponseDTO;
import com.company.erm.entity.TimeEntry;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TimeEntry entities and TimeEntry DTOs.
 */
@Component
public class TimeEntryMapper {

    /**
     * Convert TimeEntry entity to TimeEntryResponseDTO.
     */
    public TimeEntryResponseDTO toResponseDTO(TimeEntry timeEntry) {
        TimeEntryResponseDTO dto = new TimeEntryResponseDTO();
        dto.setId(timeEntry.getId());
        
        // Employee information
        if (timeEntry.getEmployee() != null) {
            dto.setEmployeeId(timeEntry.getEmployee().getId());
            dto.setEmployeeName(timeEntry.getEmployee().getFirstName() + " " + timeEntry.getEmployee().getLastName());
        }
        
        // Project information
        if (timeEntry.getProject() != null) {
            // Note: Project entity uses Long but database uses UUID
            // Setting to null for now until Project entity is fixed
            dto.setProjectId(null);
            dto.setProjectName(timeEntry.getProject().getName());
        }
        
        // Task information
        if (timeEntry.getTask() != null) {
            dto.setTaskId(timeEntry.getTask().getId());
            dto.setTaskTitle(timeEntry.getTask().getTitle());
        }
        
        dto.setEntryDate(timeEntry.getEntryDate());
        dto.setStartTime(timeEntry.getStartTime());
        dto.setEndTime(timeEntry.getEndTime());
        dto.setDurationHours(timeEntry.getDurationHours());
        dto.setDescription(timeEntry.getDescription());
        dto.setStatus(timeEntry.getStatus());
        
        // Approval information
        if (timeEntry.getApprovedBy() != null) {
            dto.setApprovedById(timeEntry.getApprovedBy().getId());
            dto.setApprovedByName(timeEntry.getApprovedBy().getFirstName() + " " + timeEntry.getApprovedBy().getLastName());
        }
        
        dto.setApprovedAt(timeEntry.getApprovedAt());
        dto.setRejectionReason(timeEntry.getRejectionReason());
        dto.setCreatedAt(timeEntry.getCreatedAt());
        dto.setUpdatedAt(timeEntry.getUpdatedAt());
        
        return dto;
    }
}

