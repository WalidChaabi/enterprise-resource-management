package com.company.erm.controller;

import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.dto.UserRoleAssignmentDTO;
import com.company.erm.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user role assignment management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Roles", description = "Assign roles to organization members")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/employees/{employeeId}/roles")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN')")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Assign a role to an employee.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN
     */
    @Operation(summary = "Assign Role")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoleResponseDTO> assignRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            @Valid @RequestBody UserRoleAssignmentDTO assignmentDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        RoleResponseDTO role = userRoleService.assignRoleToUser(
            organizationId,
            employeeId,
            assignmentDTO,
            currentUserEmail
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * Remove a role from an employee.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN
     */
    @Operation(summary = "Remove Role")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            @PathVariable UUID roleId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        userRoleService.removeRoleFromUser(
            organizationId,
            employeeId,
            roleId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }

    /**
     * List all roles assigned to an employee.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN, DEPARTMENT_MANAGER
     */
    @Operation(summary = "Get User Roles")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')")
    public ResponseEntity<List<RoleResponseDTO>> getUserRoles(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        List<RoleResponseDTO> roles = userRoleService.getUserRoles(
            organizationId,
            employeeId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(roles);
    }
}

