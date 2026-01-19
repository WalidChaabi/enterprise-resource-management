package com.company.erm.service;

import jakarta.transaction.Transactional;

import java.util.UUID;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import com.company.erm.dto.ProjectCreateDTO;
import com.company.erm.dto.ProjectUpdateDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectDomainService domainService;

    public ProjectService(
        ProjectRepository projectRepository,
        OrganizationRepository organizationRepository,
        UserRepository userRepository,
        EmployeeRepository employeeRepository,
        ProjectDomainService domainService
    ) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.domainService = domainService;
    }

    public Project create(UUID orgId, ProjectCreateDTO dto) {

        Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new BusinessException("Organization not found"));

        domainService.validateDates(dto.getStartDate(), dto.getDeadline());

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(ProjectStatus.PLANNING);
        project.setStartDate(dto.getStartDate());
        project.setDeadline(dto.getDeadline());
        project.setBudget(dto.getBudget());
        project.setOrganization(org);
        project.setArchived(false);

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new BusinessException("Manager not found"));
            project.setManager(manager);
        }

        // Add project members if provided
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            Set<Employee> members = new HashSet<>(
                employeeRepository.findByIdInAndOrganizationId(dto.getMemberIds(), orgId)
            );
            
            if (members.size() != dto.getMemberIds().size()) {
                throw new BusinessException("One or more employees not found or do not belong to the organization");
            }
            
            project.setMembers(members);
        }

        return projectRepository.save(project);
    }

    public Project getById(UUID orgId, UUID projectId) {
        return projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));
    }

    public Project update(UUID orgId, UUID projectId, ProjectUpdateDTO dto) {
        Project project = projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));

        if (dto.getName() != null) {
            project.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        if (dto.getEndDate() != null) {
            project.setEndDate(dto.getEndDate());
        }
        if (dto.getCost() != null) {
            project.setCost(dto.getCost());
        }

        return projectRepository.save(project);
    }

    public void archive(UUID orgId, UUID projectId) {
        Project project = projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));

        project.setArchived(true);
    }

    public void delete(UUID orgId, UUID projectId) {
        Project project = projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));

        projectRepository.delete(project);
    }

    public Project addMember(UUID orgId, UUID projectId, UUID employeeId) {
        Project project = projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));

        if (!employee.getOrganization().getId().equals(orgId)) {
            throw new BusinessException("Employee does not belong to the organization");
        }

        project.getMembers().add(employee);
        return projectRepository.save(project);
    }

    public Project removeMember(UUID orgId, UUID projectId, UUID employeeId) {
        Project project = projectRepository
            .findByIdAndOrganizationId(projectId, orgId)
            .orElseThrow(() -> new BusinessException("Project not found"));

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));

        if (!project.getMembers().remove(employee)) {
            throw new BusinessException("Employee is not a member of this project");
        }

        return projectRepository.save(project);
    }
}
