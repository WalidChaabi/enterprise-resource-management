package com.company.erm.dto;

import com.company.erm.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsDTO {
    private UUID projectId;
    private String projectName;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal cost;
    private BigDecimal budgetUtilization; // cost / budget * 100
    private int membersCount;
    private int tasksCount;
    private BigDecimal totalHoursLogged;
    private int completionPercentage; // Calculated based on tasks or time
}
