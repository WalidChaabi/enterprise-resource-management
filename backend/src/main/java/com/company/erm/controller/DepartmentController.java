package com.company.erm.controller;

import com.company.erm.dto.DepartmentCreateDTO;
import com.company.erm.dto.DepartmentUpdateDTO;
import com.company.erm.dto.DepartmentResponseDTO;
import com.company.erm.service.DepartmentService;
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
 * REST controller for department management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Departments", description = "Department management")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/departments")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Create a new department.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Operation(summary = "Create Department")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @PathVariable UUID organizationId,
            @Valid @RequestBody DepartmentCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        DepartmentResponseDTO created = departmentService.createDepartment(
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
     * Get department by ID.
     */
    @Operation(summary = "Get Department")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> getDepartment(
            @PathVariable UUID organizationId,
            @PathVariable UUID departmentId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        DepartmentResponseDTO department = departmentService.getDepartment(
            organizationId,
            departmentId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(department);
    }

    /**
     * Update department.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Operation(summary = "Update Department")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/{departmentId}")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable UUID organizationId,
            @PathVariable UUID departmentId,
            @Valid @RequestBody DepartmentUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        DepartmentResponseDTO updated = departmentService.updateDepartment(
            organizationId,
            departmentId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List departments with pagination and filtering.
     */
    @Operation(summary = "List Departments")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<DepartmentResponseDTO>> listDepartments(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<DepartmentResponseDTO> departments = departmentService.listDepartments(
            organizationId,
            parentId,
            active,
            pageable,
            currentUserEmail
        );
        
        return ResponseEntity.ok(departments);
    }

    /**
     * Deactivate a department.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Operation(summary = "Deactivate Department")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deactivateDepartment(
            @PathVariable UUID organizationId,
            @PathVariable UUID departmentId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        departmentService.deactivateDepartment(
            organizationId,
            departmentId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}
