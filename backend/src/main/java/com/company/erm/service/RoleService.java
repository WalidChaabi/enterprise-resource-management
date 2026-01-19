package com.company.erm.service;

import com.company.erm.dto.RoleCreateDTO;
import com.company.erm.dto.RoleUpdateDTO;
import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.entity.Role;
import com.company.erm.entity.Organization;
import com.company.erm.exception.RoleNotFoundException;
import com.company.erm.mapper.RoleMapper;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.security.PermissionChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for role management use cases.
 */
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleDomainService roleDomainService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final RoleMapper roleMapper;

    public RoleService(
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            RoleDomainService roleDomainService,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.roleDomainService = roleDomainService;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.roleMapper = roleMapper;
    }

    /**
     * Create a new role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @Transactional
    public RoleResponseDTO createRole(
            UUID organizationId,
            RoleCreateDTO createDTO,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, "ORG_ADMIN");
        
        // Load organization
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        
        // Business validation
        roleDomainService.validateRoleCreation(organizationId, createDTO.code());
        
        // Create role entity
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setOrganization(organization);
        role.setCode(createDTO.code().toUpperCase()); // Normalize to uppercase
        role.setName(createDTO.name());
        role.setDescription(createDTO.description());
        role.setScope(createDTO.scope());
        
        // Save
        Role saved = roleRepository.save(role);
        
        // Audit log
        auditLogService.logRoleCreated(
            organizationId,
            saved.getId(),
            saved.getCode(),
            currentUserEmail
        );
        
        // Map to DTO
        return roleMapper.toResponseDTO(saved);
    }

    /**
     * Update an existing role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @Transactional
    public RoleResponseDTO updateRole(
            UUID organizationId,
            UUID roleId,
            RoleUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, "ORG_ADMIN");
        
        // Load role
        Role role = roleRepository
            .findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        // Track changes for audit
        String oldName = role.getName();
        String oldScope = role.getScope();
        
        // Apply updates
        updateDTO.name().ifPresent(role::setName);
        updateDTO.description().ifPresent(role::setDescription);
        updateDTO.scope().ifPresent(role::setScope);
        
        // Business validation
        roleDomainService.validateRoleUpdate(organizationId, roleId);
        
        // Save
        Role updated = roleRepository.save(role);
        
        // Audit log
        auditLogService.logRoleUpdated(
            organizationId,
            roleId,
            currentUserEmail,
            oldName, updated.getName(),
            oldScope, updated.getScope()
        );
        
        // Map to DTO
        return roleMapper.toResponseDTO(updated);
    }

    /**
     * Get role by ID.
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO getRole(UUID organizationId, UUID roleId, String currentUserEmail) {
        
        Role role = roleRepository
            .findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        return roleMapper.toResponseDTO(role);
    }

    /**
     * List roles with pagination and filtering.
     */
    @Transactional(readOnly = true)
    public Page<RoleResponseDTO> listRoles(
            UUID organizationId,
            String scope,
            Pageable pageable,
            String currentUserEmail) {
        
        Page<Role> roles;
        
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        
        if (scope != null && !scope.isEmpty()) {
            // Filter by scope
            roles = roleRepository.findByOrganizationOrScope(organization, scope, pageable);
        } else {
            // Get all roles for organization
            roles = roleRepository.findByOrganizationId(organizationId, pageable);
        }
        
        return roles.map(roleMapper::toResponseDTO);
    }

    /**
     * Delete a role.
     * 
     * Permissions: ORG_ADMIN only
     */
    @Transactional
    public void deleteRole(
            UUID organizationId,
            UUID roleId,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail, "ORG_ADMIN");
        
        Role role = roleRepository
            .findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        // Business rule: cannot delete if assigned to users
        roleDomainService.validateRoleDeletion(organizationId, roleId);
        
        roleRepository.delete(role);
        
        auditLogService.logRoleDeleted(
            organizationId, roleId, role.getCode(), currentUserEmail);
    }
}

