package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.ProjectStatsDTO;
import com.company.erm.dto.TimeReportDTO;
import com.company.erm.entity.Project;
import com.company.erm.entity.Task;
import com.company.erm.entity.TaskStatus;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class TimeReportService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;

    public TimeReportService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TimeEntryRepository timeEntryRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
    }

    public ProjectStatsDTO getProjectStats(UUID organizationId, UUID projectId) {
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
        List<Project> projects = projectRepository.findByOrganizationId(organizationId);
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        return projects.stream()
                .map(project -> {
                    List<TimeEntry> entries = timeEntryRepository.findByProjectIdAndEntryDateBetween(
                            project.getId(), start, end);

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

    public List<TimeReportDTO> getTimeReportByEmployee(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(0, 10000);
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        return employees.stream()
                .map(employee -> {
                    List<TimeEntry> entries = timeEntryRepository.findByEmployeeIdAndEntryDateBetween(
                            employee.getId(), start, end);

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
}
