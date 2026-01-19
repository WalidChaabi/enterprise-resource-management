package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Employee allocation information for resource management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAllocationDTO {
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String jobTitle;
    private boolean active;
    
    // Current allocation
    private int currentProjectsCount;
    private List<UUID> currentProjectIds;
    private List<String> currentProjectNames;
    
    // Workload metrics (for current week)
    private BigDecimal totalHoursThisWeek;
    private BigDecimal capacityHoursPerWeek; // Default: 40 hours
    private BigDecimal utilizationPercentage; // (totalHours / capacity) * 100
    
    // Status
    private String allocationStatus; // AVAILABLE, NORMAL, OVERALLOCATED, UNDERUTILIZED
    
    // Availability
    private LocalDate availableFrom; // When employee becomes available (if overallocated)
    private String notes; // Additional notes about allocation
}
