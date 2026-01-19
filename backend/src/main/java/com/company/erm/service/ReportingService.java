package com.company.erm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.AlertDTO;
import com.company.erm.dto.AlertType;
import com.company.erm.dto.DashboardDTO;
import com.company.erm.dto.EmployeeAllocationDTO;
import com.company.erm.dto.LeaveBalanceDTO;
import com.company.erm.dto.LeaveReportDTO;
import com.company.erm.dto.ProjectAllocationDTO;
import com.company.erm.dto.ProjectStatsDTO;
import com.company.erm.dto.ResourceAllocationDTO;
import com.company.erm.dto.TimeReportDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.entity.Task;
import com.company.erm.entity.TaskStatus;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public ReportingService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get project statistics including budget, cost, hours, tasks, and members.
     */
    public ProjectStatsDTO getProjectStats(UUID organizationId, UUID projectId) {
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new BusinessException("Project not found"));

        // Get tasks for this project
        List<Task> tasks = taskRepository.findByProjectIdAndOrganizationId(projectId, organizationId);
        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        int completionPercentage = tasks.isEmpty() ? 0 : (completedTasks * 100) / tasks.size();

        // Get time entries for this project
        List<TimeEntry> timeEntries = timeEntryRepository.findByProjectId(projectId);
        BigDecimal totalHours = timeEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate budget utilization
        BigDecimal budgetUtilization = BigDecimal.ZERO;
        if (project.getBudget() != null && project.getBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cost = project.getCost() != null ? project.getCost() : BigDecimal.ZERO;
            budgetUtilization = cost.divide(project.getBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new ProjectStatsDTO(
                project.getId(),
                project.getName(),
                project.getStatus(),
                project.getStartDate(),
                project.getDeadline(),
                project.getEndDate(),
                project.getBudget(),
                project.getCost(),
                budgetUtilization,
                project.getMembers().size(),
                tasks.size(),
                totalHours,
                completionPercentage
        );
    }

    /**
     * Get time report by project.
     */
    public List<TimeReportDTO> getTimeReportByProject(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        List<Project> projects = projectRepository.findByOrganizationId(organizationId);

        return projects.stream()
                .map(project -> {
                    List<TimeEntry> entries = timeEntryRepository.findByProjectIdAndEntryDateBetween(
                            project.getId(),
                            startDate != null ? startDate : LocalDate.MIN,
                            endDate != null ? endDate : LocalDate.MAX
                    );

                    BigDecimal totalHours = entries.stream()
                            .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                            .map(TimeEntry::getDurationHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int count = (int) entries.stream()
                            .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                            .count();

                    BigDecimal avgHours = count > 0
                            ? totalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new TimeReportDTO(
                            project.getId(),
                            project.getName(),
                            totalHours,
                            count,
                            avgHours
                    );
                })
                .filter(tr -> tr.getTotalHours().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Get time report by employee.
     */
    public List<TimeReportDTO> getTimeReportByEmployee(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(0, 10000); // Large page size to get all employees
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();

        return employees.stream()
                .map(employee -> {
                    List<TimeEntry> entries = timeEntryRepository.findByEmployeeIdAndEntryDateBetween(
                            employee.getId(),
                            startDate != null ? startDate : LocalDate.MIN,
                            endDate != null ? endDate : LocalDate.MAX
                    );

                    BigDecimal totalHours = entries.stream()
                            .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                            .map(TimeEntry::getDurationHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int count = (int) entries.stream()
                            .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                            .count();

                    BigDecimal avgHours = count > 0
                            ? totalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new TimeReportDTO(
                            employee.getId(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            totalHours,
                            count,
                            avgHours
                    );
                })
                .filter(tr -> tr.getTotalHours().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Get time tracking summary.
     */
    public TimeReportDTO getTimeSummary(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> entries = timeEntryRepository.findByOrganizationId(organizationId);

        if (startDate != null || endDate != null) {
            LocalDate start = startDate != null ? startDate : LocalDate.MIN;
            LocalDate end = endDate != null ? endDate : LocalDate.MAX;
            entries = entries.stream()
                    .filter(te -> !te.getEntryDate().isBefore(start) && !te.getEntryDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        BigDecimal totalHours = entries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int count = (int) entries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                .count();

        BigDecimal avgHours = count > 0
                ? totalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new TimeReportDTO(
                organizationId,
                "Total",
                totalHours,
                count,
                avgHours
        );
    }

    /**
     * Get leave balance report for all employees.
     */
    public List<LeaveBalanceDTO> getLeaveBalance(UUID organizationId) {
        Pageable pageable = PageRequest.of(0, 10000); // Large page size to get all employees
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();

        return employees.stream()
                .map(employee -> {
                    List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getId());

                    int totalDaysUsed = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysPending = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.PENDING)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysApproved = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysThisYear = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED
                                    && lr.getStartDate().getYear() == LocalDate.now().getYear())
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    return new LeaveBalanceDTO(
                            employee.getId(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            employee.getEmail(),
                            totalDaysUsed,
                            totalDaysPending,
                            totalDaysApproved,
                            totalDaysThisYear
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming leave requests (next 30 days).
     */
    public List<LeaveReportDTO> getUpcomingLeaveRequests(UUID organizationId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);

        List<LeaveRequest> requests = leaveRequestRepository.findByOrganizationId(organizationId);

        return requests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED
                        && !lr.getStartDate().isBefore(today)
                        && !lr.getStartDate().isAfter(thirtyDaysFromNow))
                .map(lr -> new LeaveReportDTO(
                        lr.getEmployee().getId(),
                        lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                        lr.getEmployee().getEmail(),
                        lr.getId(),
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        lr.getTotalDays(),
                        lr.getStatus()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard data with overall statistics.
     */
    public DashboardDTO getDashboard(UUID organizationId) {
        // Projects (only non-archived for dashboard)
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

        // Time Tracking
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

        // Leave Management
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

        // Employees
        Pageable pageable = PageRequest.of(0, 10000); // Large page size to get all employees
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, null, pageable).getContent();
        int totalEmployees = employees.size();
        int activeEmployees = (int) employees.stream()
                .filter(User::isActive)
                .count();

        // Budget
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

        // Generate intelligent alerts
        List<AlertDTO> alerts = generateAlerts(organizationId, projects, allEntries);

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

    /**
     * Generate intelligent alerts for managers.
     */
    private List<AlertDTO> generateAlerts(UUID organizationId, List<Project> projects, List<TimeEntry> allEntries) {
        List<AlertDTO> alerts = new java.util.ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysAgo = now.minusDays(7);

        // Check each project for alerts
        for (Project project : projects) {
            if (project.isArchived()) {
                continue;
            }

            // Alert: Project without resources
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

            // Alert: Project at risk (no time entries in last 7 days for active project)
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

            // Alert: Project deadline approaching
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

            // Alert: Project over budget
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

        // Check for overallocated employees (using time entries from current week)
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        BigDecimal standardCapacity = BigDecimal.valueOf(40); // 40 hours per week

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

    /**
     * Get resource allocation view for managers.
     */
    public ResourceAllocationDTO getResourceAllocation(UUID organizationId) {
        // Get all active employees
        Pageable pageable = PageRequest.of(0, 10000);
        List<User> users = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();
        
        // Get all active projects
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        
        // Get all time entries
        List<TimeEntry> allEntries = timeEntryRepository.findByOrganizationId(organizationId);
        
        // Calculate current week
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        BigDecimal standardCapacity = BigDecimal.valueOf(40); // 40 hours per week

        // Build employee allocations
        List<EmployeeAllocationDTO> employeeAllocations = new java.util.ArrayList<>();
        
        for (User user : users) {
            if (!user.isActive()) {
                continue;
            }

            // Get employee record
            Employee employee = employeeRepository.findById(user.getId()).orElse(null);
            if (employee == null || !employee.isActive()) {
                continue;
            }

            // Get projects this employee is assigned to
            List<Project> assignedProjects = projects.stream()
                    .filter(p -> p.getMembers() != null && 
                            p.getMembers().stream()
                                    .anyMatch(m -> m.getId().equals(employee.getId())))
                    .collect(Collectors.toList());

            // Calculate hours this week
            BigDecimal totalHoursThisWeek = allEntries.stream()
                    .filter(te -> te.getEmployee().getId().equals(user.getId())
                            && te.getStatus() == TimeEntryStatus.APPROVED
                            && !te.getEntryDate().isBefore(startOfWeek)
                            && !te.getEntryDate().isAfter(now))
                    .map(TimeEntry::getDurationHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate utilization
            BigDecimal utilization = totalHoursThisWeek.divide(standardCapacity, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // Determine allocation status
            String allocationStatus;
            if (utilization.compareTo(BigDecimal.valueOf(110)) > 0) {
                allocationStatus = "OVERALLOCATED";
            } else if (utilization.compareTo(BigDecimal.valueOf(90)) < 0) {
                allocationStatus = "UNDERUTILIZED";
            } else if (utilization.compareTo(BigDecimal.valueOf(100)) <= 0) {
                allocationStatus = "NORMAL";
            } else {
                allocationStatus = "NORMAL"; // 100-110% is still acceptable
            }

            if (assignedProjects.isEmpty() && totalHoursThisWeek.compareTo(BigDecimal.ZERO) == 0) {
                allocationStatus = "AVAILABLE";
            }

            employeeAllocations.add(new EmployeeAllocationDTO(
                    employee.getId(),
                    user.getFirstName() + " " + user.getLastName(),
                    employee.getEmployeeCode(),
                    employee.getJobTitle(),
                    employee.isActive(),
                    assignedProjects.size(),
                    assignedProjects.stream().map(Project::getId).collect(Collectors.toList()),
                    assignedProjects.stream().map(Project::getName).collect(Collectors.toList()),
                    totalHoursThisWeek,
                    standardCapacity,
                    utilization,
                    allocationStatus,
                    null, // availableFrom - could be calculated from leave requests
                    null  // notes
            ));
        }

        // Build project allocations
        List<ProjectAllocationDTO> projectAllocations = new java.util.ArrayList<>();
        
        for (Project project : projects) {
            if (project.isArchived()) {
                continue;
            }

            // Get assigned members
            Set<Employee> members = project.getMembers() != null ? project.getMembers() : new HashSet<>();
            
            // Calculate allocated hours this week
            BigDecimal allocatedHoursThisWeek = allEntries.stream()
                    .filter(te -> te.getProject().getId().equals(project.getId())
                            && te.getStatus() == TimeEntryStatus.APPROVED
                            && !te.getEntryDate().isBefore(startOfWeek)
                            && !te.getEntryDate().isAfter(now))
                    .map(TimeEntry::getDurationHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Estimate required hours (simplified: assume 20h/week per member for active projects)
            BigDecimal requiredHoursPerWeek = BigDecimal.ZERO;
            if (project.getStatus() == ProjectStatus.ACTIVE && !members.isEmpty()) {
                requiredHoursPerWeek = BigDecimal.valueOf(members.size() * 20);
            }

            BigDecimal gapHours = requiredHoursPerWeek.subtract(allocatedHoursThisWeek);

            // Check for recent time entries
            boolean hasRecentTimeEntries = allEntries.stream()
                    .anyMatch(te -> te.getProject().getId().equals(project.getId())
                            && !te.getEntryDate().isBefore(now.minusDays(7))
                            && te.getStatus() == TimeEntryStatus.APPROVED);
            
            int daysSinceLastEntry = 0;
            if (!allEntries.isEmpty()) {
                Optional<TimeEntry> lastEntry = allEntries.stream()
                        .filter(te -> te.getProject().getId().equals(project.getId())
                                && te.getStatus() == TimeEntryStatus.APPROVED)
                        .max(Comparator.comparing(TimeEntry::getEntryDate));
                
                if (lastEntry.isPresent()) {
                    daysSinceLastEntry = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            lastEntry.get().getEntryDate(), now);
                }
            }

            // Determine allocation status
            String allocationStatus;
            if (members.isEmpty()) {
                allocationStatus = "NO_RESOURCES";
            } else if (gapHours.compareTo(BigDecimal.valueOf(10)) > 0) {
                allocationStatus = "UNDERSTAFFED";
            } else if (gapHours.compareTo(BigDecimal.valueOf(-10)) < 0) {
                allocationStatus = "OVERSTAFFED";
            } else {
                allocationStatus = "WELL_STAFFED";
            }

            // Budget utilization
            BigDecimal budgetUtilizationPercentage = BigDecimal.ZERO;
            if (project.getBudget() != null && project.getBudget().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cost = project.getCost() != null ? project.getCost() : BigDecimal.ZERO;
                budgetUtilizationPercentage = cost.divide(project.getBudget(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            projectAllocations.add(new ProjectAllocationDTO(
                    project.getId(),
                    project.getName(),
                    project.getStatus() != null ? project.getStatus().toString() : null,
                    project.getStartDate(),
                    project.getEndDate(),
                    project.getDeadline(),
                    members.size(),
                    members.stream().map(Employee::getId).collect(Collectors.toList()),
                    members.stream()
                            .map(e -> {
                                User u = userRepository.findById(e.getId()).orElse(null);
                                return u != null ? u.getFirstName() + " " + u.getLastName() : "Unknown";
                            })
                            .collect(Collectors.toList()),
                    requiredHoursPerWeek,
                    allocatedHoursThisWeek,
                    gapHours,
                    project.getBudget(),
                    project.getCost(),
                    budgetUtilizationPercentage,
                    allocationStatus,
                    hasRecentTimeEntries,
                    daysSinceLastEntry,
                    project.getDeadline() != null && 
                            java.time.temporal.ChronoUnit.DAYS.between(now, project.getDeadline()) <= 7,
                    project.getBudget() != null && project.getCost() != null &&
                            project.getCost().compareTo(project.getBudget()) > 0
            ));
        }

        // Calculate summary metrics
        int availableEmployees = (int) employeeAllocations.stream()
                .filter(ea -> "AVAILABLE".equals(ea.getAllocationStatus()))
                .count();
        int overallocatedEmployees = (int) employeeAllocations.stream()
                .filter(ea -> "OVERALLOCATED".equals(ea.getAllocationStatus()))
                .count();
        int underutilizedEmployees = (int) employeeAllocations.stream()
                .filter(ea -> "UNDERUTILIZED".equals(ea.getAllocationStatus()))
                .count();

        int wellStaffedProjects = (int) projectAllocations.stream()
                .filter(pa -> "WELL_STAFFED".equals(pa.getAllocationStatus()))
                .count();
        int understaffedProjects = (int) projectAllocations.stream()
                .filter(pa -> "UNDERSTAFFED".equals(pa.getAllocationStatus()))
                .count();
        int projectsWithoutResources = (int) projectAllocations.stream()
                .filter(pa -> "NO_RESOURCES".equals(pa.getAllocationStatus()))
                .count();

        return new ResourceAllocationDTO(
                employeeAllocations,
                projectAllocations,
                employeeAllocations.size(),
                availableEmployees,
                overallocatedEmployees,
                underutilizedEmployees,
                projectAllocations.size(),
                wellStaffedProjects,
                understaffedProjects,
                projectsWithoutResources
        );
    }
}
