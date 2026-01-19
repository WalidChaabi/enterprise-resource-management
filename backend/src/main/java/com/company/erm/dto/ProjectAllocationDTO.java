package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Project allocation information for resource management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAllocationDTO {
    private UUID projectId;
    private String projectName;
    private String projectStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    
    // Resource allocation
    private int assignedMembersCount;
    private List<UUID> assignedMemberIds;
    private List<String> assignedMemberNames;
    
    // Workload metrics
    private BigDecimal requiredHoursPerWeek; // Estimated hours needed per week
    private BigDecimal allocatedHoursPerWeek; // Actual hours allocated (from time entries)
    private BigDecimal gapHours; // required - allocated (negative = overallocated)
    
    // Budget metrics
    private BigDecimal budget;
    private BigDecimal cost;
    private BigDecimal budgetUtilizationPercentage;
    
    // Status
    private String allocationStatus; // WELL_STAFFED, UNDERSTAFFED, OVERSTAFFED, NO_RESOURCES
    
    // Risk indicators
    private boolean hasRecentTimeEntries; // Time entries in last 7 days
    private int daysSinceLastTimeEntry;
    private boolean deadlineApproaching; // Within 7 days
    private boolean isOverBudget;
}
