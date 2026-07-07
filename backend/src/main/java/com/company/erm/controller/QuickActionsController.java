package com.company.erm.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.company.erm.dto.DepartmentCreateDTO;
import com.company.erm.dto.DepartmentResponseDTO;
import com.company.erm.dto.EmployeeCreateDTO;
import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.dto.LeaveRequestCreateDTO;
import com.company.erm.dto.LeaveRequestResponseDTO;
import com.company.erm.dto.ProjectCreateDTO;
import com.company.erm.dto.ProjectResponseDTO;
import com.company.erm.dto.QuickProjectCreateDTO;
import com.company.erm.dto.TimeEntryCreateDTO;
import com.company.erm.dto.TimeEntryResponseDTO;
import com.company.erm.entity.User;
import com.company.erm.mapper.ProjectMapper;
import com.company.erm.security.OrganizationSecurityService;
import com.company.erm.service.CurrentUserService;
import com.company.erm.service.DepartmentService;
import com.company.erm.service.EmployeeService;
import com.company.erm.service.LeaveRequestService;
import com.company.erm.service.ProjectService;
import com.company.erm.service.TimeEntryService;

import jakarta.validation.Valid;

/**
 * Quick action endpoints for creating common entities.
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Quick Actions", description = "Bulk approval shortcuts")
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class QuickActionsController {

    private final CurrentUserService currentUserService;
    private final OrganizationSecurityService organizationSecurity;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final TimeEntryService timeEntryService;
    private final LeaveRequestService leaveRequestService;
    private final ProjectMapper projectMapper;

    public QuickActionsController(
            CurrentUserService currentUserService,
            OrganizationSecurityService organizationSecurity,
            EmployeeService employeeService,
            ProjectService projectService,
            DepartmentService departmentService,
            TimeEntryService timeEntryService,
            LeaveRequestService leaveRequestService,
            ProjectMapper projectMapper) {
        this.currentUserService = currentUserService;
        this.organizationSecurity = organizationSecurity;
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.departmentService = departmentService;
        this.timeEntryService = timeEntryService;
        this.leaveRequestService = leaveRequestService;
        this.projectMapper = projectMapper;
    }

    @Operation(summary = "Create Employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('HR_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        UUID organizationId = organizationSecurity.requireJwtOrganizationId();
        EmployeeResponseDTO created = employeeService.createEmployee(
            organizationId,
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Create Project")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody QuickProjectCreateDTO dto,
            Authentication authentication) {
        UUID organizationId = organizationSecurity.requireJwtOrganizationId();

        ProjectCreateDTO createDTO = new ProjectCreateDTO();
        createDTO.setName(dto.getName());
        createDTO.setDescription(dto.getDescription());
        createDTO.setStartDate(dto.getStartDate());
        createDTO.setDeadline(dto.getDeadline());
        createDTO.setBudget(dto.getBudget());
        createDTO.setManagerId(dto.getManagerId());
        createDTO.setMemberIds(dto.getMemberIds());

        ProjectResponseDTO created = projectMapper.toDto(
            projectService.create(organizationId, createDTO)
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Create Department")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('HR_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @Valid @RequestBody DepartmentCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        UUID organizationId = organizationSecurity.requireJwtOrganizationId();
        DepartmentResponseDTO created = departmentService.createDepartment(
            organizationId,
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Create Time Entry")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/time-entries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<TimeEntryResponseDTO> createTimeEntry(
            @Valid @RequestBody TimeEntryCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        UUID organizationId = organizationSecurity.requireJwtOrganizationId();
        TimeEntryResponseDTO created = timeEntryService.createTimeEntry(
            organizationId,
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Create Leave Request")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/leave-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN', 'HR_ADMIN', 'DEPARTMENT_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveRequestResponseDTO> createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        UUID organizationId = organizationSecurity.requireJwtOrganizationId();
        LeaveRequestResponseDTO created = leaveRequestService.createLeaveRequest(
            organizationId,
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
