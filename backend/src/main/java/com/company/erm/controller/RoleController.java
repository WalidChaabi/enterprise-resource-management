package com.company.erm.controller;

import com.company.erm.dto.RoleCreateDTO;
import com.company.erm.dto.RoleUpdateDTO;
import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.service.RoleService;
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
 * REST controller for role management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/roles")
@PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a new role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORG_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoleResponseDTO> createRole(
            @PathVariable UUID organizationId,
            @Valid @RequestBody RoleCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        RoleResponseDTO created = roleService.createRole(
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
     * Get role by ID.
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponseDTO> getRole(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("roleId") UUID roleId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        RoleResponseDTO role = roleService.getRole(
            organizationId,
            roleId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(role);
    }

    /**
     * Update role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ORG_ADMIN')")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        RoleResponseDTO updated = roleService.updateRole(
            organizationId,
            roleId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List roles with pagination and filtering.
     */
    @GetMapping
    public ResponseEntity<Page<RoleResponseDTO>> listRoles(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) String scope,
            Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<RoleResponseDTO> roles = roleService.listRoles(
            organizationId,
            scope,
            pageable,
            currentUserEmail
        );
        
        return ResponseEntity.ok(roles);
    }

    /**
     * Delete a role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ORG_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID roleId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        roleService.deleteRole(
            organizationId,
            roleId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}

