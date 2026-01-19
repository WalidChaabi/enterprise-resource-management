package com.company.erm.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.DashboardDTO;
import com.company.erm.dto.LeaveBalanceDTO;
import com.company.erm.dto.LeaveReportDTO;
import com.company.erm.dto.ProjectStatsDTO;
import com.company.erm.dto.ResourceAllocationDTO;
import com.company.erm.dto.TimeReportDTO;
import com.company.erm.service.ReportingService;

/**
 * REST controller for reporting and analytics.
 * 
 * Provides endpoints for:
 * - Project statistics
 * - Time tracking reports
 * - Leave management reports
 * - Dashboard data
 * 
 * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/reports")
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * Get dashboard with overall statistics.
     * 
     * Returns aggregated data for projects, time tracking, leave management, employees, and budget.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard(@PathVariable UUID organizationId) {
        DashboardDTO dashboard = reportingService.getDashboard(organizationId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get project statistics.
     * 
     * Returns detailed statistics for a specific project including:
     * - Budget vs cost
     * - Member count
     * - Task count and completion percentage
     * - Total hours logged
     */
    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<ProjectStatsDTO> getProjectStats(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId) {
        ProjectStatsDTO stats = reportingService.getProjectStats(organizationId, projectId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get time report by project.
     * 
     * Returns time tracking data grouped by project.
     * 
     * @param startDate Optional start date filter (format: YYYY-MM-DD)
     * @param endDate Optional end date filter (format: YYYY-MM-DD)
     */
    @GetMapping("/time/by-project")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByProject(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TimeReportDTO> report = reportingService.getTimeReportByProject(organizationId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get time report by employee.
     * 
     * Returns time tracking data grouped by employee.
     * 
     * @param startDate Optional start date filter (format: YYYY-MM-DD)
     * @param endDate Optional end date filter (format: YYYY-MM-DD)
     */
    @GetMapping("/time/by-employee")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByEmployee(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TimeReportDTO> report = reportingService.getTimeReportByEmployee(organizationId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get time tracking summary.
     * 
     * Returns overall time tracking statistics.
     * 
     * @param startDate Optional start date filter (format: YYYY-MM-DD)
     * @param endDate Optional end date filter (format: YYYY-MM-DD)
     */
    @GetMapping("/time/summary")
    public ResponseEntity<TimeReportDTO> getTimeSummary(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TimeReportDTO summary = reportingService.getTimeSummary(organizationId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get leave balance report.
     * 
     * Returns leave balance information for all employees including:
     * - Total days used
     * - Days pending approval
     * - Days approved
     * - Days used this year
     */
    @GetMapping("/leave/balance")
    public ResponseEntity<List<LeaveBalanceDTO>> getLeaveBalance(@PathVariable UUID organizationId) {
        List<LeaveBalanceDTO> balance = reportingService.getLeaveBalance(organizationId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Get upcoming leave requests.
     * 
     * Returns approved leave requests for the next 30 days.
     */
    @GetMapping("/leave/upcoming")
    public ResponseEntity<List<LeaveReportDTO>> getUpcomingLeaveRequests(@PathVariable UUID organizationId) {
        List<LeaveReportDTO> upcoming = reportingService.getUpcomingLeaveRequests(organizationId);
        return ResponseEntity.ok(upcoming);
    }

    /**
     * Get resource allocation view.
     * 
     * Returns detailed resource allocation information including:
     * - Employee workload and utilization
     * - Project staffing status
     * - Overallocation and underutilization alerts
     * - Resource gaps and availability
     */
    @GetMapping("/resource-allocation")
    public ResponseEntity<ResourceAllocationDTO> getResourceAllocation(@PathVariable UUID organizationId) {
        ResourceAllocationDTO allocation = reportingService.getResourceAllocation(organizationId);
        return ResponseEntity.ok(allocation);
    }
}
