package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.ProjectStatsDTO;
import com.company.erm.dto.TimeReportDTO;
import com.company.erm.entity.Project;
import com.company.erm.entity.Task;
import com.company.erm.entity.TaskStatus;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.reporting.EmployeeHoursAggregate;
import com.company.erm.repository.reporting.ProjectHoursAggregate;
import com.company.erm.security.OrganizationSecurityService;

@Service
@Transactional(readOnly = true)
public class TimeReportService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final OrganizationSecurityService organizationSecurity;

    public TimeReportService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TimeEntryRepository timeEntryRepository,
            UserRepository userRepository,
            OrganizationSecurityService organizationSecurity) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.organizationSecurity = organizationSecurity;
    }

    public ProjectStatsDTO getProjectStats(UUID organizationId, UUID projectId) {
        organizationSecurity.requireOrgMembership(organizationId);
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new BusinessException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectIdAndOrganizationId(projectId, organizationId);
        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        int completionPercentage = tasks.isEmpty() ? 0 : (completedTasks * 100) / tasks.size();

        List<TimeEntry> timeEntries = timeEntryRepository.findByProjectId(projectId);
        BigDecimal totalHours = timeEntries.stream()
                .filter(te -> te.getStatus() == TimeEntryStatus.APPROVED)
                .map(TimeEntry::getDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

    public List<TimeReportDTO> getTimeReportByProject(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        Map<UUID, Project> projectsById = projectRepository.findByOrganizationId(organizationId).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        return timeEntryRepository.sumApprovedHoursByProjectInRange(
                        organizationId, TimeEntryStatus.APPROVED, start, end)
                .stream()
                .filter(row -> row.getTotalHours().compareTo(BigDecimal.ZERO) > 0)
                .map(row -> toProjectTimeReport(row, projectsById.get(row.getProjectId())))
                .collect(Collectors.toList());
    }

    public List<TimeReportDTO> getTimeReportByEmployee(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        Map<UUID, String> namesByUserId = userRepository.findActiveByOrganizationId(organizationId).stream()
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getFirstName() + " " + user.getLastName()));

        return timeEntryRepository.sumApprovedHoursByEmployeeInRange(
                        organizationId, TimeEntryStatus.APPROVED, start, end)
                .stream()
                .filter(row -> row.getTotalHours().compareTo(BigDecimal.ZERO) > 0)
                .map(row -> toEmployeeTimeReport(row, namesByUserId.get(row.getEmployeeId())))
                .collect(Collectors.toList());
    }

    public TimeReportDTO getTimeSummary(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        BigDecimal totalHours = timeEntryRepository.sumHoursByOrganizationStatusAndDateRange(
                organizationId, TimeEntryStatus.APPROVED, start, end);
        long count = timeEntryRepository.countByOrganizationIdAndStatusAndDateRange(
                organizationId, TimeEntryStatus.APPROVED, start, end);

        BigDecimal avgHours = count > 0
                ? totalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new TimeReportDTO(
                organizationId,
                "Total",
                totalHours,
                (int) count,
                avgHours
        );
    }

    private TimeReportDTO toProjectTimeReport(ProjectHoursAggregate row, Project project) {
        String name = project != null ? project.getName() : "Unknown project";
        return toTimeReportDto(row.getProjectId(), name, row.getTotalHours(), row.getEntryCount());
    }

    private TimeReportDTO toEmployeeTimeReport(EmployeeHoursAggregate row, String name) {
        String displayName = name != null ? name : "Unknown employee";
        return toTimeReportDto(row.getEmployeeId(), displayName, row.getTotalHours(), row.getEntryCount());
    }

    private TimeReportDTO toTimeReportDto(UUID id, String name, BigDecimal totalHours, long count) {
        BigDecimal avgHours = count > 0
                ? totalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new TimeReportDTO(id, name, totalHours, (int) count, avgHours);
    }
}
