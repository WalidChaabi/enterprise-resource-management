package com.company.erm.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.CapacityDashboardDTO;
import com.company.erm.dto.DashboardDTO;
import com.company.erm.dto.LeaveBalanceDTO;
import com.company.erm.dto.LeaveReportDTO;
import com.company.erm.dto.ProjectStatsDTO;
import com.company.erm.dto.ResourceAllocationDTO;
import com.company.erm.dto.TimeReportDTO;
import com.company.erm.entitlement.PlanEntitlement;
import com.company.erm.entity.Organization;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.security.OrganizationSecurityService;
import com.company.erm.security.RoleCode;
import com.company.erm.service.EntitlementService;
import com.company.erm.service.reporting.CapacityDashboardService;
import com.company.erm.service.reporting.DashboardService;
import com.company.erm.service.reporting.LeaveReportService;
import com.company.erm.service.reporting.ResourceAllocationService;
import com.company.erm.service.reporting.TimeReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for reporting and analytics.
 *
 * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN
 */
@Tag(name = "Reporting", description = "Dashboards, time/leave reports, capacity, and resource allocation")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/reports")
public class ReportingController {

    private static final RoleCode[] REPORTING_ROLES = {
            RoleCode.ORG_ADMIN,
            RoleCode.PROJECT_MANAGER,
            RoleCode.HR_ADMIN
    };

    private final DashboardService dashboardService;
    private final TimeReportService timeReportService;
    private final LeaveReportService leaveReportService;
    private final ResourceAllocationService resourceAllocationService;
    private final CapacityDashboardService capacityDashboardService;
    private final OrganizationRepository organizationRepository;
    private final EntitlementService entitlementService;
    private final OrganizationSecurityService organizationSecurity;

    public ReportingController(
            DashboardService dashboardService,
            TimeReportService timeReportService,
            LeaveReportService leaveReportService,
            ResourceAllocationService resourceAllocationService,
            CapacityDashboardService capacityDashboardService,
            OrganizationRepository organizationRepository,
            EntitlementService entitlementService,
            OrganizationSecurityService organizationSecurity) {
        this.dashboardService = dashboardService;
        this.timeReportService = timeReportService;
        this.leaveReportService = leaveReportService;
        this.resourceAllocationService = resourceAllocationService;
        this.capacityDashboardService = capacityDashboardService;
        this.organizationRepository = organizationRepository;
        this.entitlementService = entitlementService;
        this.organizationSecurity = organizationSecurity;
    }

    private void requireReporting(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        entitlementService.requireEntitlement(organization, PlanEntitlement.REPORTING);
    }

    @Operation(summary = "Get organization or employee dashboard")
    @ApiResponse(responseCode = "200", description = "Dashboard metrics")
    @GetMapping("/dashboard")
    @PreAuthorize("@organizationSecurity.hasOrgAccess(#organizationId)")
    public ResponseEntity<DashboardDTO> getDashboard(
            @PathVariable UUID organizationId,
            Authentication authentication) {
        String email = authentication.getName();
        if (organizationSecurity.hasAnyOrgRole(email, organizationId, REPORTING_ROLES)) {
            return ResponseEntity.ok(dashboardService.getDashboard(organizationId));
        }
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard(organizationId, email));
    }

    @Operation(summary = "Get project statistics")
    @ApiResponse(responseCode = "200", description = "Project stats")
    @GetMapping("/projects/{projectId}/stats")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<ProjectStatsDTO> getProjectStats(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId) {
        requireReporting(organizationId);
        return ResponseEntity.ok(timeReportService.getProjectStats(organizationId, projectId));
    }

    @Operation(summary = "Get approved time grouped by project")
    @ApiResponse(responseCode = "200", description = "Time report by project")
    @GetMapping("/time/by-project")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByProject(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        requireReporting(organizationId);
        return ResponseEntity.ok(timeReportService.getTimeReportByProject(organizationId, startDate, endDate));
    }

    @Operation(summary = "Get approved time grouped by employee")
    @ApiResponse(responseCode = "200", description = "Time report by employee")
    @GetMapping("/time/by-employee")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<List<TimeReportDTO>> getTimeReportByEmployee(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        requireReporting(organizationId);
        return ResponseEntity.ok(timeReportService.getTimeReportByEmployee(organizationId, startDate, endDate));
    }

    @Operation(summary = "Get organization time summary")
    @ApiResponse(responseCode = "200", description = "Time summary")
    @GetMapping("/time/summary")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<TimeReportDTO> getTimeSummary(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        requireReporting(organizationId);
        return ResponseEntity.ok(timeReportService.getTimeSummary(organizationId, startDate, endDate));
    }

    @Operation(summary = "Get leave balance per employee")
    @ApiResponse(responseCode = "200", description = "Leave balances")
    @GetMapping("/leave/balance")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<List<LeaveBalanceDTO>> getLeaveBalance(@PathVariable UUID organizationId) {
        requireReporting(organizationId);
        return ResponseEntity.ok(leaveReportService.getLeaveBalance(organizationId));
    }

    @Operation(summary = "Get upcoming approved leave requests")
    @ApiResponse(responseCode = "200", description = "Upcoming leave")
    @GetMapping("/leave/upcoming")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<List<LeaveReportDTO>> getUpcomingLeaveRequests(@PathVariable UUID organizationId) {
        requireReporting(organizationId);
        return ResponseEntity.ok(leaveReportService.getUpcomingLeaveRequests(organizationId));
    }

    @Operation(summary = "Get resource allocation overview")
    @ApiResponse(responseCode = "200", description = "Resource allocation")
    @GetMapping("/resource-allocation")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<ResourceAllocationDTO> getResourceAllocation(@PathVariable UUID organizationId) {
        requireReporting(organizationId);
        return ResponseEntity.ok(resourceAllocationService.getResourceAllocation(organizationId));
    }

    @Operation(summary = "Get weekly capacity dashboard")
    @ApiResponse(responseCode = "200", description = "Capacity dashboard")
    @GetMapping("/capacity")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<CapacityDashboardDTO> getCapacityDashboard(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        requireReporting(organizationId);
        return ResponseEntity.ok(capacityDashboardService.getCapacityDashboard(organizationId, weekStart));
    }
}
