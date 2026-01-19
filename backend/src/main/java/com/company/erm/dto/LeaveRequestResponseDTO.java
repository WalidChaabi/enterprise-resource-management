package com.company.erm.dto;

import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeaveRequestResponseDTO {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private UUID departmentId;
    private String departmentName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveRequestStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

