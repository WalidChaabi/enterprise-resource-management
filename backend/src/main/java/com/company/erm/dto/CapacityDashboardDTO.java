package com.company.erm.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityDashboardDTO {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<EmployeeCapacityRowDTO> employees;
    private int totalEmployees;
    private int overallocatedEmployees;
    private int availableEmployees;
    private int underutilizedEmployees;
    private int onLeaveEmployees;
}
