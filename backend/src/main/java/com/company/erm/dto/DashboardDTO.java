package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    // Projects
    private int totalProjects;
    private int activeProjects;
    private int completedProjects;
    private int projectsOnHold;
    
    // Time Tracking
    private BigDecimal totalHoursThisMonth;
    private BigDecimal totalHoursThisWeek;
    private int pendingTimeEntries;
    private int approvedTimeEntries;
    
    // Leave Management
    private int pendingLeaveRequests;
    private int approvedLeaveRequests;
    private int upcomingLeaveRequests; // Next 30 days
    
    // Employees
    private int totalEmployees;
    private int activeEmployees;
    
    // Budget
    private BigDecimal totalBudget;
    private BigDecimal totalCost;
    private BigDecimal budgetUtilization; // Percentage
    
    // Intelligent Alerts
    private List<AlertDTO> alerts;
}
