package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.AlertDTO;
import com.company.erm.dto.AlertType;
import com.company.erm.dto.DashboardDTO;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.entity.Employee;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.reporting.EmployeeHoursAggregate;
import com.company.erm.security.OrganizationSecurityService;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationSecurityService organizationSecurity;

    public DashboardService(
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            OrganizationSecurityService organizationSecurity) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.organizationSecurity = organizationSecurity;
    }

    @CacheEvict(value = "dashboard", key = "#organizationId")
    public void evictDashboard(UUID organizationId) {
        // Cache eviction handled by annotation.
    }

    @Cacheable(value = "dashboard", key = "#organizationId + ':org'")
    public DashboardDTO getDashboard(UUID organizationId) {
        organizationSecurity.requireOrgMembership(organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        int totalProjects = (int) projectRepository.countByOrganizationIdAndArchivedFalse(organizationId);
        int activeProjects = (int) projectRepository.countByOrganizationIdAndArchivedFalseAndStatus(
                organizationId, ProjectStatus.ACTIVE);
        int completedProjects = (int) projectRepository.countByOrganizationIdAndArchivedFalseAndStatus(
                organizationId, ProjectStatus.COMPLETED);
        int projectsOnHold = (int) projectRepository.countByOrganizationIdAndArchivedFalseAndStatus(
                organizationId, ProjectStatus.ON_HOLD);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);

        BigDecimal totalHoursThisMonth = timeEntryRepository.sumHoursByOrganizationStatusAndDateRange(
                organizationId, TimeEntryStatus.APPROVED, startOfMonth, now);
        BigDecimal totalHoursThisWeek = timeEntryRepository.sumHoursByOrganizationStatusAndDateRange(
                organizationId, TimeEntryStatus.APPROVED, startOfWeek, now);
        int pendingTimeEntries = (int) timeEntryRepository.countByOrganizationIdAndStatus(
                organizationId, TimeEntryStatus.SUBMITTED);
        int approvedTimeEntries = (int) timeEntryRepository.countByOrganizationIdAndStatus(
                organizationId, TimeEntryStatus.APPROVED);

        int pendingLeaveRequests = (int) leaveRequestRepository.countByOrganizationIdAndStatus(
                organizationId, LeaveRequestStatus.PENDING);
        int approvedLeaveRequests = (int) leaveRequestRepository.countByOrganizationIdAndStatus(
                organizationId, LeaveRequestStatus.APPROVED);

        LocalDate thirtyDaysFromNow = now.plusDays(30);
        int upcomingLeaveRequests = (int) leaveRequestRepository.countUpcomingApprovedLeave(
                organizationId, now, thirtyDaysFromNow);

        int totalEmployees = (int) userRepository.countByOrganizationId(organizationId);
        int activeEmployees = (int) userRepository.countByOrganizationIdAndActiveTrue(organizationId);

        BigDecimal totalBudget = projectRepository.sumBudgetByOrganizationIdAndArchivedFalse(organizationId);
        BigDecimal totalCost = projectRepository.sumCostByOrganizationIdAndArchivedFalse(organizationId);

        BigDecimal budgetUtilization = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilization = totalCost.divide(totalBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<EmployeeHoursAggregate> weeklyHours = timeEntryRepository.sumApprovedHoursByEmployeeInRange(
                organizationId, TimeEntryStatus.APPROVED, startOfWeek, now);
        Set<UUID> projectsWithRecentEntries = new HashSet<>(
                timeEntryRepository.findProjectIdsWithApprovedEntriesSince(
                        organizationId, now.minusDays(7)));

        List<AlertDTO> alerts = generateAlerts(projects, projectsWithRecentEntries, weeklyHours);

        return new DashboardDTO(
                totalProjects,
                activeProjects,
                completedProjects,
                projectsOnHold,
                totalHoursThisMonth,
                totalHoursThisWeek,
                pendingTimeEntries,
                approvedTimeEntries,
                pendingLeaveRequests,
                approvedLeaveRequests,
                upcomingLeaveRequests,
                totalEmployees,
                activeEmployees,
                totalBudget,
                totalCost,
                budgetUtilization,
                alerts
        );
    }

    @Cacheable(value = "dashboard", key = "#organizationId + ':' + #userEmail")
    public DashboardDTO getEmployeeDashboard(UUID organizationId, String userEmail) {
        organizationSecurity.requireOrgMembership(userEmail, organizationId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Employee profile not found"));

        UUID employeeId = employee.getId();
        List<Project> memberProjects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId).stream()
                .filter(project -> project.getMembers() != null
                        && project.getMembers().stream().anyMatch(member -> member.getId().equals(employeeId)))
                .toList();

        int totalProjects = memberProjects.size();
        int activeProjects = (int) memberProjects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.ACTIVE)
                .count();
        int completedProjects = (int) memberProjects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.COMPLETED)
                .count();
        int projectsOnHold = (int) memberProjects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.ON_HOLD)
                .count();

        List<TimeEntry> myEntries = timeEntryRepository.findByEmployeeId(employeeId);
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);

        BigDecimal totalHoursThisMonth = sumApprovedHours(myEntries, startOfMonth, now);
        BigDecimal totalHoursThisWeek = sumApprovedHours(myEntries, startOfWeek, now);
        int pendingTimeEntries = (int) myEntries.stream()
                .filter(entry -> entry.getStatus() == TimeEntryStatus.SUBMITTED)
                .count();
        int approvedTimeEntries = (int) myEntries.stream()
                .filter(entry -> entry.getStatus() == TimeEntryStatus.APPROVED)
                .count();

        List<LeaveRequest> myLeaveRequests = leaveRequestRepository.findByEmployeeId(employeeId);
        int pendingLeaveRequests = (int) myLeaveRequests.stream()
                .filter(request -> request.getStatus() == LeaveRequestStatus.PENDING)
                .count();
        int approvedLeaveRequests = (int) myLeaveRequests.stream()
                .filter(request -> request.getStatus() == LeaveRequestStatus.APPROVED)
                .count();

        LocalDate thirtyDaysFromNow = now.plusDays(30);
        int upcomingLeaveRequests = (int) myLeaveRequests.stream()
                .filter(request -> request.getStatus() == LeaveRequestStatus.APPROVED
                        && !request.getStartDate().isBefore(now)
                        && !request.getStartDate().isAfter(thirtyDaysFromNow))
                .count();

        return new DashboardDTO(
                totalProjects,
                activeProjects,
                completedProjects,
                projectsOnHold,
                totalHoursThisMonth,
                totalHoursThisWeek,
                pendingTimeEntries,
                approvedTimeEntries,
                pendingLeaveRequests,
                approvedLeaveRequests,
                upcomingLeaveRequests,
                1,
                user.isActive() ? 1 : 0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of()
        );
    }

    private BigDecimal sumApprovedHours(List<TimeEntry> entries, LocalDate startDate, LocalDate endDate) {
        return entries.stream()
                .filter(entry -> entry.getStatus() == TimeEntryStatus.APPROVED
                        && !entry.getEntryDate().isBefore(startDate)
                        && !entry.getEntryDate().isAfter(endDate))
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AlertDTO> generateAlerts(
            List<Project> projects,
            Set<UUID> projectsWithRecentEntries,
            List<EmployeeHoursAggregate> weeklyHours) {
        List<AlertDTO> alerts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        BigDecimal standardCapacity = BigDecimal.valueOf(40);

        for (Project project : projects) {
            if (project.isArchived()) {
                continue;
            }

            if (project.getMembers() == null || project.getMembers().isEmpty()) {
                alerts.add(new AlertDTO(
                        AlertType.PROJECT_WITHOUT_RESOURCES,
                        "HIGH",
                        String.format("Project '%s' has no assigned members", project.getName()),
                        project.getId(),
                        project.getName(),
                        null,
                        null,
                        "Assign at least one member to this project"
                ));
            }

            if (project.getStatus() == ProjectStatus.ACTIVE) {
                boolean hasRecentEntries = projectsWithRecentEntries.contains(project.getId());

                if (!hasRecentEntries && (project.getMembers() == null || !project.getMembers().isEmpty())) {
                    alerts.add(new AlertDTO(
                            AlertType.PROJECT_AT_RISK,
                            "MEDIUM",
                            String.format("Project '%s' has no time entries in the last 7 days", project.getName()),
                            project.getId(),
                            project.getName(),
                            null,
                            null,
                            "Check if project is still active or if team members are logging time"
                    ));
                }
            }

            if (project.getDeadline() != null) {
                long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(now, project.getDeadline());
                if (daysUntilDeadline >= 0 && daysUntilDeadline <= 7 && project.getStatus() == ProjectStatus.ACTIVE) {
                    alerts.add(new AlertDTO(
                            AlertType.PROJECT_DEADLINE_APPROACHING,
                            "MEDIUM",
                            String.format("Project '%s' deadline is in %d days", project.getName(), daysUntilDeadline),
                            project.getId(),
                            project.getName(),
                            null,
                            null,
                            String.format("Deadline: %s", project.getDeadline())
                    ));
                }
            }

            if (project.getBudget() != null && project.getCost() != null) {
                if (project.getCost().compareTo(project.getBudget()) > 0) {
                    BigDecimal overage = project.getCost().subtract(project.getBudget());
                    alerts.add(new AlertDTO(
                            AlertType.PROJECT_OVER_BUDGET,
                            "HIGH",
                            String.format("Project '%s' is over budget by %s", project.getName(), overage),
                            project.getId(),
                            project.getName(),
                            null,
                            null,
                            String.format("Budget: %s, Cost: %s", project.getBudget(), project.getCost())
                    ));
                }
            }
        }

        for (EmployeeHoursAggregate aggregate : weeklyHours) {
            BigDecimal hours = aggregate.getTotalHours();
            if (hours.compareTo(standardCapacity) > 0) {
                BigDecimal overage = hours.subtract(standardCapacity);
                BigDecimal utilization = hours.divide(standardCapacity, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                User employee = userRepository.findById(aggregate.getEmployeeId()).orElse(null);
                if (employee != null) {
                    alerts.add(new AlertDTO(
                            AlertType.EMPLOYEE_OVERALLOCATED,
                            "HIGH",
                            String.format("%s is overallocated (%.0f%% utilization)",
                                    employee.getFirstName() + " " + employee.getLastName(),
                                    utilization),
                            null,
                            null,
                            employee.getId(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            String.format("Working %.1f hours/week (%.1f hours over capacity)",
                                    hours, overage)
                    ));
                }
            }
        }

        return alerts;
    }
}
