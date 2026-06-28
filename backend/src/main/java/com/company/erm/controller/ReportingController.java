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
import com.company.erm.service.reporting.DashboardService;
import com.company.erm.service.reporting.LeaveReportService;
import com.company.erm.service.reporting.ResourceAllocationService;
import com.company.erm.service.reporting.TimeReportService;

/**
 * REST controller for reporting and analytics.
 *
 * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/reports")
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
public class ReportingController {

    private final DashboardService dashboardService;
    private final TimeReportService timeReportService;
    private final LeaveReportService leaveReportService;
    private final ResourceAllocationService resourceAllocationService;

    public ReportingController(
            DashboardService dashboardService,
            TimeReportService timeReportService,
            LeaveReportService leaveReportService,
            ResourceAllocationService resourceAllocationService) {
        this.dashboardService = dashboardService;
        this.timeReportService = timeReportService;
        this.leaveReportService = leaveReportService;
        this.resourceAllocationService = resourceAllocationService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(dashboardService.getDashboard(organizationId));
    }

    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<ProjectStatsDTO> getProjectStats(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(timeReportService.getProjectStats(organizationId, projectId));
    }

    @GetMapping("/time/by-project")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByProject(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeReportService.getTimeReportByProject(organizationId, startDate, endDate));
    }

    @GetMapping("/time/by-employee")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByEmployee(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeReportService.getTimeReportByEmployee(organizationId, startDate, endDate));
    }

    @GetMapping("/time/summary")
    public ResponseEntity<TimeReportDTO> getTimeSummary(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeReportService.getTimeSummary(organizationId, startDate, endDate));
    }

    @GetMapping("/leave/balance")
    public ResponseEntity<List<LeaveBalanceDTO>> getLeaveBalance(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(leaveReportService.getLeaveBalance(organizationId));
    }

    @GetMapping("/leave/upcoming")
    public ResponseEntity<List<LeaveReportDTO>> getUpcomingLeaveRequests(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(leaveReportService.getUpcomingLeaveRequests(organizationId));
    }

    @GetMapping("/resource-allocation")
    public ResponseEntity<ResourceAllocationDTO> getResourceAllocation(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(resourceAllocationService.getResourceAllocation(organizationId));
    }
}
