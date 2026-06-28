package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.EmployeeAllocationDTO;
import com.company.erm.dto.ProjectAllocationDTO;
import com.company.erm.dto.ResourceAllocationDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ResourceAllocationService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;

    public ResourceAllocationService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public ResourceAllocationDTO getResourceAllocation(UUID organizationId) {
        Pageable pageable = PageRequest.of(0, 10000);
        List<User> users = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();

        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        List<TimeEntry> allEntries = timeEntryRepository.findByOrganizationId(organizationId);

        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        BigDecimal standardCapacity = BigDecimal.valueOf(40);

        List<EmployeeAllocationDTO> employeeAllocations = new ArrayList<>();

        for (User user : users) {
            if (!user.isActive()) {
                continue;
            }

            Employee employee = employeeRepository.findById(user.getId()).orElse(null);
            if (employee == null || !employee.isActive()) {
                continue;
            }

            List<Project> assignedProjects = projects.stream()
                    .filter(p -> p.getMembers() != null
                            && p.getMembers().stream()
                                    .anyMatch(m -> m.getId().equals(employee.getId())))
                    .collect(Collectors.toList());

            BigDecimal totalHoursThisWeek = allEntries.stream()
                    .filter(te -> te.getEmployee().getId().equals(user.getId())
                            && te.getStatus() == TimeEntryStatus.APPROVED
                            && !te.getEntryDate().isBefore(startOfWeek)
                            && !te.getEntryDate().isAfter(now))
                    .map(TimeEntry::getDurationHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal utilization = totalHoursThisWeek.divide(standardCapacity, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            String allocationStatus;
            if (utilization.compareTo(BigDecimal.valueOf(110)) > 0) {
                allocationStatus = "OVERALLOCATED";
            } else if (utilization.compareTo(BigDecimal.valueOf(90)) < 0) {
                allocationStatus = "UNDERUTILIZED";
            } else if (utilization.compareTo(BigDecimal.valueOf(100)) <= 0) {
                allocationStatus = "NORMAL";
            } else {
                allocationStatus = "NORMAL";
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
                    null,
                    null
            ));
        }

        List<ProjectAllocationDTO> projectAllocations = new ArrayList<>();

        for (Project project : projects) {
            if (project.isArchived()) {
                continue;
            }

            Set<Employee> members = project.getMembers() != null ? project.getMembers() : new HashSet<>();

            BigDecimal allocatedHoursThisWeek = allEntries.stream()
                    .filter(te -> te.getProject().getId().equals(project.getId())
                            && te.getStatus() == TimeEntryStatus.APPROVED
                            && !te.getEntryDate().isBefore(startOfWeek)
                            && !te.getEntryDate().isAfter(now))
                    .map(TimeEntry::getDurationHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal requiredHoursPerWeek = BigDecimal.ZERO;
            if (project.getStatus() == ProjectStatus.ACTIVE && !members.isEmpty()) {
                requiredHoursPerWeek = BigDecimal.valueOf(members.size() * 20);
            }

            BigDecimal gapHours = requiredHoursPerWeek.subtract(allocatedHoursThisWeek);

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
                    project.getDeadline() != null
                            && java.time.temporal.ChronoUnit.DAYS.between(now, project.getDeadline()) <= 7,
                    project.getBudget() != null && project.getCost() != null
                            && project.getCost().compareTo(project.getBudget()) > 0
            ));
        }

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
