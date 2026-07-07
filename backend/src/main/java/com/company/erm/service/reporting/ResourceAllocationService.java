package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.EmployeeAllocationDTO;
import com.company.erm.dto.ProjectAllocationDTO;
import com.company.erm.dto.ResourceAllocationDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.entity.User;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.reporting.EmployeeHoursAggregate;
import com.company.erm.repository.reporting.ProjectHoursAggregate;
import com.company.erm.repository.reporting.ProjectLastActivityAggregate;
import com.company.erm.security.OrganizationSecurityService;

@Service
@Transactional(readOnly = true)
public class ResourceAllocationService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final OrganizationSecurityService organizationSecurity;

    public ResourceAllocationService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            OrganizationSecurityService organizationSecurity) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.organizationSecurity = organizationSecurity;
    }

    public ResourceAllocationDTO getResourceAllocation(UUID organizationId) {
        organizationSecurity.requireOrgMembership(organizationId);
        List<User> users = userRepository.findActiveByOrganizationId(organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);

        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        BigDecimal standardCapacity = BigDecimal.valueOf(40);

        Map<UUID, BigDecimal> hoursByUserId = timeEntryRepository.sumApprovedHoursByEmployeeInRange(
                        organizationId, TimeEntryStatus.APPROVED, startOfWeek, now)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeHoursAggregate::getEmployeeId,
                        EmployeeHoursAggregate::getTotalHours));
        Map<UUID, BigDecimal> hoursByProjectId = timeEntryRepository.sumApprovedHoursByProjectInRange(
                        organizationId, TimeEntryStatus.APPROVED, startOfWeek, now)
                .stream()
                .collect(Collectors.toMap(
                        ProjectHoursAggregate::getProjectId,
                        ProjectHoursAggregate::getTotalHours));
        Set<UUID> projectsWithRecentEntries = new HashSet<>(
                timeEntryRepository.findProjectIdsWithApprovedEntriesSince(organizationId, now.minusDays(7)));
        Map<UUID, LocalDate> lastEntryByProjectId = timeEntryRepository.findLastApprovedEntryDateByProject(organizationId)
                .stream()
                .collect(Collectors.toMap(
                        ProjectLastActivityAggregate::getProjectId,
                        ProjectLastActivityAggregate::getLastEntryDate));

        List<EmployeeAllocationDTO> employeeAllocations = new ArrayList<>();

        for (User user : users) {
            Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
            if (employee == null || !employee.isActive()) {
                continue;
            }

            List<Project> assignedProjects = projects.stream()
                    .filter(p -> p.getMembers() != null
                            && p.getMembers().stream()
                                    .anyMatch(m -> m.getId().equals(employee.getId())))
                    .collect(Collectors.toList());

            BigDecimal totalHoursThisWeek = hoursByUserId.getOrDefault(user.getId(), BigDecimal.ZERO);
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

            BigDecimal allocatedHoursThisWeek = hoursByProjectId.getOrDefault(project.getId(), BigDecimal.ZERO);

            BigDecimal requiredHoursPerWeek = BigDecimal.ZERO;
            if (project.getStatus() == ProjectStatus.ACTIVE && !members.isEmpty()) {
                requiredHoursPerWeek = BigDecimal.valueOf(members.size() * 20);
            }

            BigDecimal gapHours = requiredHoursPerWeek.subtract(allocatedHoursThisWeek);
            boolean hasRecentTimeEntries = projectsWithRecentEntries.contains(project.getId());

            int daysSinceLastEntry = 0;
            LocalDate lastEntryDate = lastEntryByProjectId.get(project.getId());
            if (lastEntryDate != null) {
                daysSinceLastEntry = (int) java.time.temporal.ChronoUnit.DAYS.between(lastEntryDate, now);
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
                                User u = e.getUser();
                                if (u == null) {
                                    return "Unknown";
                                }
                                return u.getFirstName() + " " + u.getLastName();
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
