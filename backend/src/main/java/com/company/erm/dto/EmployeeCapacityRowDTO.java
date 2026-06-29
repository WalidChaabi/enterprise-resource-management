package com.company.erm.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCapacityRowDTO {
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String jobTitle;
    private BigDecimal loggedHours;
    private BigDecimal capacityHoursPerWeek;
    private BigDecimal utilizationPercentage;
    private String allocationStatus;
    private boolean onLeaveThisWeek;
    private int leaveDaysThisWeek;
    private String leaveSummary;
}
