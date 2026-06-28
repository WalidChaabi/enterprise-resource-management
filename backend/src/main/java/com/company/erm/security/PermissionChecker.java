package com.company.erm.security;

import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for checking user permissions and roles.
 */
@Service
public class PermissionChecker {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    
    public PermissionChecker(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }
    
    public void requireRole(UUID organizationId, String userEmail, RoleCode... roles) {
        requireRole(organizationId, userEmail,
                Arrays.stream(roles).map(RoleCode::code).toArray(String[]::new));
    }

    /**
     * Require user to have one of the specified roles in the organization.
     * 
     * @throws AccessDeniedException if user doesn't have required role
     */
    public void requireRole(UUID organizationId, String userEmail, String... roles) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        if (!user.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("User does not belong to organization");
        }
        
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        boolean hasRole = userRoles.stream()
            .anyMatch(ur -> 
                ur.getOrganization().getId().equals(organizationId) &&
                Arrays.asList(roles).contains(ur.getRole().getCode())
            );
        
        if (!hasRole) {
            throw new AccessDeniedException(
                "User does not have required role: " + String.join(", ", roles));
        }
    }
    
    /**
     * Check if user has one of the specified roles.
     * 
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(UUID organizationId, String userEmail, String... roles) {
        try {
            requireRole(organizationId, userEmail, roles);
            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }
    
    /**
     * Require user to have access to the specified department.
     * Managers must manage the department, or user must have HR_ADMIN/ORG_ADMIN role.
     * 
     * @throws AccessDeniedException if user doesn't have access
     */
    public void requireDepartmentAccess(
            UUID organizationId, 
            String userEmail, 
            UUID departmentId) {
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        // HR_ADMIN and ORG_ADMIN have access to all departments
        boolean isHrAdmin = hasRole(organizationId, userEmail,
                RoleCode.HR_ADMIN.code(), RoleCode.ORG_ADMIN.code());
        if (isHrAdmin) {
            return;
        }
        
        // Department managers can only access their own department
        if (user.getDepartment() != null && user.getDepartment().getId().equals(departmentId)) {
            boolean isManager = hasRole(organizationId, userEmail, RoleCode.DEPARTMENT_MANAGER.code());
            if (isManager) {
                return;
            }
        }
        
        throw new AccessDeniedException(
            "User does not have access to department: " + departmentId);
    }
    
    /**
     * Get the department ID that a manager manages.
     * 
     * @return department ID if user is a manager, null otherwise
     */
    public UUID getUserDepartmentId(UUID organizationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        if (user.getDepartment() != null) {
            return user.getDepartment().getId();
        }
        
        return null;
    }
}

