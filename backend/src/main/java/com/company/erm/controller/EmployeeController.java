package com.company.erm.controller;

import com.company.erm.dto.EmployeeCreateDTO;
import com.company.erm.dto.EmployeeUpdateDTO;
import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for employee management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Employees", description = "Employee profiles")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/employees")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Create a new employee.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     * 
     * @param organizationId Organization ID (validated against authenticated user's org)
     * @param createDTO Employee creation data
     * @param authentication Current authenticated user
     * @return Created employee with 201 status
     */
    @Operation(summary = "Create Employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @PathVariable UUID organizationId,
            @Valid @RequestBody EmployeeCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO created = employeeService.createEmployee(
            organizationId, 
            createDTO, 
            currentUserEmail
        );
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Get employee by ID.
     * 
     * Permissions: 
     * - HR_ADMIN, ORG_ADMIN: any employee in organization
     * - DEPARTMENT_MANAGER: employees in their department only
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param authentication Current authenticated user
     * @return Employee details
     */
    @Operation(summary = "Get Employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO employee = employeeService.getEmployee(
            organizationId, 
            employeeId, 
            currentUserEmail
        );
        
        return ResponseEntity.ok(employee);
    }

    /**
     * Update employee.
     * 
     * Permissions:
     * - HR_ADMIN, ORG_ADMIN: any employee in organization
     * - DEPARTMENT_MANAGER: employees in their department only (limited fields)
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param updateDTO Update data
     * @param authentication Current authenticated user
     * @return Updated employee
     */
    @Operation(summary = "Update Employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO updated = employeeService.updateEmployee(
            organizationId,
            employeeId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List employees with pagination and filtering.
     * 
     * Permissions:
     * - HR_ADMIN, ORG_ADMIN: all employees in organization
     * - DEPARTMENT_MANAGER: employees in their department only
     * 
     * @param organizationId Organization ID
     * @param departmentId Optional department filter
     * @param active Optional active status filter
     * @param pageable Pagination parameters
     * @param authentication Current authenticated user
     * @return Paginated list of employees
     */
    @Operation(summary = "List Employees")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> listEmployees(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<EmployeeResponseDTO> employees = employeeService.listEmployees(
            organizationId,
            departmentId,
            active,
            pageable,
            currentUserEmail
        );
        
        return ResponseEntity.ok(employees);
    }

    /**
     * Deactivate an employee.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param authentication Current authenticated user
     * @return 204 No Content
     */
    @Operation(summary = "Deactivate Employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        employeeService.deactivateEmployee(
            organizationId,
            employeeId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}

