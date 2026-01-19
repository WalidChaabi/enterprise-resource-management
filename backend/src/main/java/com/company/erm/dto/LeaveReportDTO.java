package com.company.erm.dto;

import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveReportDTO {
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private UUID leaveRequestId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private LeaveRequestStatus status;
}
