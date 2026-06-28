package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public DashboardService(
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "dashboard", key = "#organizationId")
    public void evictDashboard(UUID organizationId) {
        // Cache eviction handled by annotation.
    }

    @Cacheable(value = "dashboard", key = "#organizationId")
    public DashboardDTO getDashboard(UUID organizationId) {
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        int totalProjects = projects.size();
        int activeProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
                .count();
        int completedProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();
        int projectsOnHold = (int) projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ON_HOLD)
                .count();

        List<TimeEntry> allEntries = timeEntryRepository.findByOrganizationId(organizationId);
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);

        BigDecimal totalHoursThisMonth = allEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED
                        && !te.getEntryDate().isBefore(startOfMonth)
                        && !te.getEntryDate().isAfter(now))
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHoursThisWeek = allEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED
                        && !te.getEntryDate().isBefore(startOfWeek)
                        && !te.getEntryDate().isAfter(now))
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int pendingTimeEntries = (int) allEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.SUBMITTED)
                .count();

        int approvedTimeEntries = (int) allEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                .count();

        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByOrganizationId(organizationId);
        int pendingLeaveRequests = (int) leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.PENDING)
                .count();

        int approvedLeaveRequests = (int) leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                .count();

        LocalDate thirtyDaysFromNow = now.plusDays(30);
        int upcomingLeaveRequests = (int) leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED
                        && !lr.getStartDate().isBefore(now)
                        && !lr.getStartDate().isAfter(thirtyDaysFromNow))
                .count();

        Pageable pageable = PageRequest.of(0, 10000);
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, null, pageable).getContent();
        int totalEmployees = employees.size();
        int activeEmployees = (int) employees.stream()
                .filter(User::isActive)
                .count();

        BigDecimal totalBudget = projects.stream()
                .map(Project::getBudget)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = projects.stream()
                .map(Project::getCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetUtilization = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilization = totalCost.divide(totalBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<AlertDTO> alerts = generateAlerts(projects, allEntries);

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

    private List<AlertDTO> generateAlerts(List<Project> projects, List<TimeEntry> allEntries) {
        List<AlertDTO> alerts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysAgo = now.minusDays(7);

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
                boolean hasRecentEntries = allEntries.stream()
                        .anyMatch(te -> te.getProject().getId().equals(project.getId())
                                && !te.getEntryDate().isBefore(sevenDaysAgo)
                                && te.getStatus() == TimeEntryStatus.APPROVED);

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

        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        BigDecimal standardCapacity = BigDecimal.valueOf(40);

        Map<UUID, BigDecimal> employeeHours = allEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED
                        && !te.getEntryDate().isBefore(startOfWeek)
                        && !te.getEntryDate().isAfter(now))
                .collect(Collectors.groupingBy(
                        te -> te.getEmployee().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                TimeEntry::getDurationHours,
                                BigDecimal::add
                        )
                ));

        for (Map.Entry<UUID, BigDecimal> entry : employeeHours.entrySet()) {
            if (entry.getValue().compareTo(standardCapacity) > 0) {
                BigDecimal overage = entry.getValue().subtract(standardCapacity);
                BigDecimal utilization = entry.getValue().divide(standardCapacity, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                User employee = userRepository.findById(entry.getKey()).orElse(null);
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
                                    entry.getValue(), overage)
                    ));
                }
            }
        }

        return alerts;
    }
}
