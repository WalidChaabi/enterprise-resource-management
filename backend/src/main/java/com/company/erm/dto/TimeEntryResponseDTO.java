package com.company.erm.dto;

import com.company.erm.entity.TimeEntryStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class TimeEntryResponseDTO {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private UUID projectId;
    private String projectName;
    private UUID taskId;
    private String taskTitle;
    private LocalDate entryDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal durationHours;
    private String description;
    private TimeEntryStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

