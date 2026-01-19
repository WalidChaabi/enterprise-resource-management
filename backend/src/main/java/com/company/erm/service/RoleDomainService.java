package com.company.erm.service;

import com.company.erm.entity.Role;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.RoleAlreadyExistsException;
import com.company.erm.exception.RoleNotFoundException;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain service for role business rules and validations.
 */
@Service
@Transactional
public class RoleDomainService {
    
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleRepository userRoleRepository;
    
    public RoleDomainService(
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.userRoleRepository = userRoleRepository;
    }
    
    /**
     * Validate role creation business rules.
     */
    public void validateRoleCreation(UUID organizationId, String code) {
        // Check code uniqueness within organization
        organizationRepository.findById(organizationId)
            .ifPresent(org -> {
                if (roleRepository.findByOrganizationAndCode(org, code).isPresent()) {
                    throw new RoleAlreadyExistsException(code);
                }
            });
    }
    
    /**
     * Validate role update.
     */
    public void validateRoleUpdate(UUID organizationId, UUID roleId) {
        // Ensure role exists and belongs to organization
        roleRepository.findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
    }
    
    /**
     * Validate that a role can be deleted/deactivated.
     */
    public void validateRoleDeletion(UUID organizationId, UUID roleId) {
        Role role = roleRepository.findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        // Check if role is assigned to any users
        long assignedUsers = userRoleRepository.findAll().stream()
            .filter(ur -> ur.getRole().getId().equals(roleId) &&
                         ur.getOrganization().getId().equals(organizationId))
            .count();
        
        if (assignedUsers > 0) {
            throw new BusinessException(
                "ROLE_IN_USE",
                "Cannot delete role that is assigned to " + assignedUsers + " user(s). Remove assignments first.",
                HttpStatus.CONFLICT
            );
        }
        
        // Prevent deletion of system roles (optional - can be configured)
        if (isSystemRole(role.getCode())) {
            throw new BusinessException(
                "SYSTEM_ROLE_PROTECTED",
                "Cannot delete system role: " + role.getCode(),
                HttpStatus.FORBIDDEN
            );
        }
    }
    
    /**
     * Check if a role code is a system role (protected from deletion).
     */
    private boolean isSystemRole(String code) {
        return code.equals("ADMIN") || 
               code.equals("USER") || 
               code.equals("HR_ADMIN") || 
               code.equals("ORG_ADMIN") || 
               code.equals("DEPARTMENT_MANAGER");
    }
}

