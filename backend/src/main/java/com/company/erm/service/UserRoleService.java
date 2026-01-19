package com.company.erm.service;

import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.dto.UserRoleAssignmentDTO;
import com.company.erm.entity.User;
import com.company.erm.entity.Role;
import com.company.erm.entity.Organization;
import com.company.erm.entity.UserRole;
import com.company.erm.entity.UserRoleId;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.EmployeeNotFoundException;
import com.company.erm.exception.RoleNotFoundException;
import com.company.erm.mapper.RoleMapper;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.PermissionChecker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user role assignments.
 */
@Service
@Transactional
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final RoleMapper roleMapper;

    public UserRoleService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            UserRoleRepository userRoleRepository,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            RoleMapper roleMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.roleMapper = roleMapper;
    }

    /**
     * Assign a role to a user.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN
     */
    @Transactional
    public RoleResponseDTO assignRoleToUser(
            UUID organizationId,
            UUID userId,
            UserRoleAssignmentDTO assignmentDTO,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, "ORG_ADMIN", "HR_ADMIN");
        
        // Load entities
        User user = userRepository
            .findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(userId));
        
        Role role = roleRepository
            .findByIdAndOrganizationId(assignmentDTO.roleId(), organizationId)
            .orElseThrow(() -> new RoleNotFoundException(assignmentDTO.roleId()));
        
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        
        // Check if role is already assigned
        if (userRoleRepository.findByUserIdAndRoleIdAndOrganizationId(
                userId, assignmentDTO.roleId(), organizationId).isPresent()) {
            throw new BusinessException(
                "ROLE_ALREADY_ASSIGNED",
                "Role is already assigned to this user",
                HttpStatus.CONFLICT
            );
        }
        
        // Create user role assignment
        UserRoleId userRoleId = new UserRoleId(userId, assignmentDTO.roleId(), organizationId);
        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setOrganization(organization);
        
        userRoleRepository.save(userRole);
        
        // Audit log
        auditLogService.logRoleAssigned(
            organizationId,
            userId,
            user.getEmail(),
            assignmentDTO.roleId(),
            role.getCode(),
            currentUserEmail
        );
        
        return roleMapper.toResponseDTO(role);
    }

    /**
     * Remove a role from a user.
     * 
     * Permissions: ORG_ADMIN, HR_ADMIN
     */
    @Transactional
    public void removeRoleFromUser(
            UUID organizationId,
            UUID userId,
            UUID roleId,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, "ORG_ADMIN", "HR_ADMIN");
        
        // Load user
        User user = userRepository
            .findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(userId));
        
        // Load role
        Role role = roleRepository
            .findByIdAndOrganizationId(roleId, organizationId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        // Find user role assignment
        UserRole userRole = userRoleRepository
            .findByUserIdAndRoleIdAndOrganizationId(userId, roleId, organizationId)
            .orElseThrow(() -> new BusinessException(
                "ROLE_NOT_ASSIGNED",
                "Role is not assigned to this user",
                HttpStatus.NOT_FOUND
            ));
        
        // Prevent removal of last role (business rule)
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
        if (userRoles.size() <= 1) {
            throw new BusinessException(
                "CANNOT_REMOVE_LAST_ROLE",
                "Cannot remove the last role from a user. User must have at least one role.",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Prevent removal of system roles (optional - can be configured)
        if (isSystemRole(role.getCode())) {
            throw new BusinessException(
                "SYSTEM_ROLE_PROTECTED",
                "Cannot remove system role: " + role.getCode(),
                HttpStatus.FORBIDDEN
            );
        }
        
        userRoleRepository.delete(userRole);
        
        // Audit log
        auditLogService.logRoleRemoved(
            organizationId,
            userId,
            user.getEmail(),
            roleId,
            role.getCode(),
            currentUserEmail
        );
    }

    /**
     * List all roles assigned to a user.
     */
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getUserRoles(
            UUID organizationId,
            UUID userId,
            String currentUserEmail) {
        
        // Verify user exists
        userRepository.findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(userId));
        
        // Get user roles
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
        
        return userRoles.stream()
            .map(ur -> roleMapper.toResponseDTO(ur.getRole()))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a role code is a system role (protected from removal).
     */
    private boolean isSystemRole(String code) {
        return code.equals("ADMIN") || 
               code.equals("USER") || 
               code.equals("HR_ADMIN") || 
               code.equals("ORG_ADMIN") || 
               code.equals("DEPARTMENT_MANAGER");
    }
}

