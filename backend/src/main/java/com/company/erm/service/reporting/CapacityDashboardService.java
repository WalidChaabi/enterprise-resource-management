package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.CapacityDashboardDTO;
import com.company.erm.dto.EmployeeCapacityRowDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveType;
import com.company.erm.entity.Project;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.reporting.EmployeeHoursAggregate;
import com.company.erm.security.OrganizationSecurityService;

@Service
@Transactional(readOnly = true)
public class CapacityDashboardService {

    private static final BigDecimal STANDARD_CAPACITY = BigDecimal.valueOf(40);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OrganizationSecurityService organizationSecurity;

    public CapacityDashboardService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            OrganizationSecurityService organizationSecurity) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.organizationSecurity = organizationSecurity;
    }

    public CapacityDashboardDTO getCapacityDashboard(UUID organizationId, LocalDate weekStart) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate resolvedWeekStart = resolveWeekStart(weekStart);
        LocalDate weekEnd = resolvedWeekStart.plusDays(6);

        List<User> users = userRepository.findActiveByOrganizationId(organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        Map<UUID, BigDecimal> hoursByUserId = timeEntryRepository.sumApprovedHoursByEmployeeInRange(
                        organizationId, TimeEntryStatus.APPROVED, resolvedWeekStart, weekEnd)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeHoursAggregate::getEmployeeId,
                        EmployeeHoursAggregate::getTotalHours));
        Map<UUID, List<LeaveRequest>> leaveByUserId = leaveRequestRepository
                .findApprovedLeaveOverlappingWeek(organizationId, resolvedWeekStart, weekEnd)
                .stream()
                .collect(Collectors.groupingBy(lr -> lr.getEmployee().getId()));

        List<EmployeeCapacityRowDTO> rows = new ArrayList<>();

        for (User user : users) {
            Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
            if (employee == null || !employee.isActive()) {
                continue;
            }

            Set<UUID> assignedProjectIds = projects.stream()
                    .filter(p -> p.getMembers() != null
                            && p.getMembers().stream().anyMatch(m -> m.getId().equals(employee.getId())))
                    .map(Project::getId)
                    .collect(Collectors.toSet());

            BigDecimal loggedHours = hoursByUserId.getOrDefault(user.getId(), BigDecimal.ZERO);
            BigDecimal utilization = loggedHours.divide(STANDARD_CAPACITY, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            String allocationStatus = resolveAllocationStatus(utilization, assignedProjectIds.isEmpty(), loggedHours);

            List<LeaveRequest> employeeLeave = leaveByUserId.getOrDefault(user.getId(), List.of());
            int leaveDays = employeeLeave.stream()
                    .mapToInt(lr -> countOverlapDays(lr.getStartDate(), lr.getEndDate(), resolvedWeekStart, weekEnd))
                    .sum();

            boolean onLeave = leaveDays > 0;
            String leaveSummary = buildLeaveSummary(employeeLeave, resolvedWeekStart, weekEnd);

            rows.add(new EmployeeCapacityRowDTO(
                    employee.getId(),
                    user.getFirstName() + " " + user.getLastName(),
                    employee.getEmployeeCode(),
                    employee.getJobTitle(),
                    loggedHours,
                    STANDARD_CAPACITY,
                    utilization.setScale(1, RoundingMode.HALF_UP),
                    allocationStatus,
                    onLeave,
                    leaveDays,
                    leaveSummary
            ));
        }

        rows.sort((a, b) -> b.getUtilizationPercentage().compareTo(a.getUtilizationPercentage()));

        int onLeaveCount = (int) rows.stream().filter(EmployeeCapacityRowDTO::isOnLeaveThisWeek).count();

        return new CapacityDashboardDTO(
                resolvedWeekStart,
                weekEnd,
                rows,
                rows.size(),
                (int) rows.stream().filter(r -> "OVERALLOCATED".equals(r.getAllocationStatus())).count(),
                (int) rows.stream().filter(r -> "AVAILABLE".equals(r.getAllocationStatus())).count(),
                (int) rows.stream().filter(r -> "UNDERUTILIZED".equals(r.getAllocationStatus())).count(),
                onLeaveCount
        );
    }

    private LocalDate resolveWeekStart(LocalDate weekStart) {
        LocalDate reference = weekStart != null ? weekStart : LocalDate.now();
        return reference.minusDays(reference.getDayOfWeek().getValue() - 1L);
    }

    private String resolveAllocationStatus(
            BigDecimal utilization,
            boolean noProjects,
            BigDecimal loggedHours) {
        if (noProjects && loggedHours.compareTo(BigDecimal.ZERO) == 0) {
            return "AVAILABLE";
        }
        if (utilization.compareTo(BigDecimal.valueOf(110)) > 0) {
            return "OVERALLOCATED";
        }
        if (utilization.compareTo(BigDecimal.valueOf(90)) < 0) {
            return "UNDERUTILIZED";
        }
        return "NORMAL";
    }

    private int countOverlapDays(LocalDate leaveStart, LocalDate leaveEnd, LocalDate weekStart, LocalDate weekEnd) {
        LocalDate overlapStart = leaveStart.isBefore(weekStart) ? weekStart : leaveStart;
        LocalDate overlapEnd = leaveEnd.isAfter(weekEnd) ? weekEnd : leaveEnd;
        if (overlapStart.isAfter(overlapEnd)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }

    private String buildLeaveSummary(List<LeaveRequest> leaveRequests, LocalDate weekStart, LocalDate weekEnd) {
        if (leaveRequests.isEmpty()) {
            return "";
        }
        return leaveRequests.stream()
                .map(lr -> formatLeaveType(lr.getLeaveType()) + " "
                        + formatRange(lr.getStartDate(), lr.getEndDate(), weekStart, weekEnd))
                .collect(Collectors.joining("; "));
    }

    private String formatLeaveType(LeaveType type) {
        if (type == null) {
            return "Leave";
        }
        return switch (type) {
            case VACATION -> "Vacation";
            case SICK_LEAVE -> "Sick";
            case PERSONAL -> "Personal";
            case UNPAID -> "Unpaid";
            case MATERNITY -> "Maternity";
            case PATERNITY -> "Paternity";
            case COMPASSIONATE -> "Compassionate";
            case OTHER -> "Other";
        };
    }

    private String formatRange(LocalDate start, LocalDate end, LocalDate weekStart, LocalDate weekEnd) {
        LocalDate displayStart = start.isBefore(weekStart) ? weekStart : start;
        LocalDate displayEnd = end.isAfter(weekEnd) ? weekEnd : end;
        if (displayStart.equals(displayEnd)) {
            return displayStart.toString();
        }
        return displayStart + "–" + displayEnd;
    }
}
