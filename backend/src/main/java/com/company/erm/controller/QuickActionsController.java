package com.company.erm.controller;

import java.net.URI;
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
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class QuickActionsController {

    private final CurrentUserService currentUserService;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final TimeEntryService timeEntryService;
    private final LeaveRequestService leaveRequestService;
    private final ProjectMapper projectMapper;

    public QuickActionsController(
            CurrentUserService currentUserService,
            EmployeeService employeeService,
            ProjectService projectService,
            DepartmentService departmentService,
            TimeEntryService timeEntryService,
            LeaveRequestService leaveRequestService,
            ProjectMapper projectMapper) {
        this.currentUserService = currentUserService;
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.departmentService = departmentService;
        this.timeEntryService = timeEntryService;
        this.leaveRequestService = leaveRequestService;
        this.projectMapper = projectMapper;
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        EmployeeResponseDTO created = employeeService.createEmployee(
            currentUser.getOrganization().getId(),
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody QuickProjectCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);

        ProjectCreateDTO createDTO = new ProjectCreateDTO();
        createDTO.setName(dto.getName());
        createDTO.setDescription(dto.getDescription());
        createDTO.setStartDate(dto.getStartDate());
        createDTO.setDeadline(dto.getDeadline());
        createDTO.setBudget(dto.getBudget());
        createDTO.setManagerId(dto.getManagerId());
        createDTO.setMemberIds(dto.getMemberIds());

        ProjectResponseDTO created = projectMapper.toDto(
            projectService.create(currentUser.getOrganization().getId(), createDTO)
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @Valid @RequestBody DepartmentCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        DepartmentResponseDTO created = departmentService.createDepartment(
            currentUser.getOrganization().getId(),
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/time-entries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<TimeEntryResponseDTO> createTimeEntry(
            @Valid @RequestBody TimeEntryCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        TimeEntryResponseDTO created = timeEntryService.createTimeEntry(
            currentUser.getOrganization().getId(),
            dto,
            currentUser.getEmail()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/leave-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'HR_ADMIN', 'DEPARTMENT_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveRequestResponseDTO> createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateDTO dto,
            Authentication authentication) {
        User currentUser = currentUserService.requireUser(authentication);
        LeaveRequestResponseDTO created = leaveRequestService.createLeaveRequest(
            currentUser.getOrganization().getId(),
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
