package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private Integer totalDaysUsed;
    private Integer totalDaysPending;
    private Integer totalDaysApproved;
    private Integer totalDaysThisYear;
}
