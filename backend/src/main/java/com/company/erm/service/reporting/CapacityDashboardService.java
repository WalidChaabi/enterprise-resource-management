package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.CapacityDashboardDTO;
import com.company.erm.dto.EmployeeCapacityRowDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import com.company.erm.entity.Project;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class CapacityDashboardService {

    private static final BigDecimal STANDARD_CAPACITY = BigDecimal.valueOf(40);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public CapacityDashboardService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public CapacityDashboardDTO getCapacityDashboard(UUID organizationId, LocalDate weekStart) {
        LocalDate resolvedWeekStart = resolveWeekStart(weekStart);
        LocalDate weekEnd = resolvedWeekStart.plusDays(6);

        Pageable pageable = PageRequest.of(0, 10000);
        List<User> users = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        List<TimeEntry> allEntries = timeEntryRepository.findByOrganizationId(organizationId);
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByOrganizationId(organizationId);

        List<EmployeeCapacityRowDTO> rows = new ArrayList<>();

        for (User user : users) {
            if (!user.isActive()) {
                continue;
            }

            Employee employee = employeeRepository.findById(user.getId()).orElse(null);
            if (employee == null || !employee.isActive()) {
                continue;
            }

            Set<UUID> assignedProjectIds = projects.stream()
                    .filter(p -> p.getMembers() != null
                            && p.getMembers().stream().anyMatch(m -> m.getId().equals(employee.getId())))
                    .map(Project::getId)
                    .collect(Collectors.toSet());

            BigDecimal loggedHours = allEntries.stream()
                    .filter(te -> te.getEmployee().getId().equals(user.getId())
                            && te.getStatus() == TimeEntryStatus.APPROVED
                            && !te.getEntryDate().isBefore(resolvedWeekStart)
                            && !te.getEntryDate().isAfter(weekEnd))
                    .map(TimeEntry::getDurationHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal utilization = loggedHours.divide(STANDARD_CAPACITY, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            String allocationStatus = resolveAllocationStatus(utilization, assignedProjectIds.isEmpty(), loggedHours);

            List<LeaveRequest> employeeLeave = leaveRequests.stream()
                    .filter(lr -> lr.getEmployee().getId().equals(user.getId())
                            && lr.getStatus() == LeaveRequestStatus.APPROVED
                            && !lr.getEndDate().isBefore(resolvedWeekStart)
                            && !lr.getStartDate().isAfter(weekEnd))
                    .toList();

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
