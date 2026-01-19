package com.company.erm.service;

import com.company.erm.entity.Department;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.entity.UserRoleId;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.DepartmentNotFoundException;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain service for employee business rules and validations.
 */
@Service
@Transactional
public class EmployeeDomainService {
    
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    public EmployeeDomainService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
    
    /**
     * Validate employee creation business rules.
     */
    public void validateEmployeeCreation(
            UUID organizationId,
            String email,
            UUID departmentId,
            UUID defaultRoleId) {
        
        // Check email uniqueness
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(
                "EMAIL_ALREADY_EXISTS",
                "Email already exists in organization",
                HttpStatus.CONFLICT
            );
        }
        
        // Validate department
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        if (!department.isActive()) {
            throw new BusinessException(
                "DEPARTMENT_INACTIVE",
                "Cannot assign employee to inactive department",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate role
        Role role = roleRepository.findById(defaultRoleId)
            .orElseThrow(() -> new BusinessException(
                "ROLE_NOT_FOUND",
                "Role not found with id: " + defaultRoleId,
                HttpStatus.NOT_FOUND
            ));
        
        if (role.getOrganization() != null && 
            !role.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException(
                "ROLE_NOT_IN_ORGANIZATION",
                "Role does not belong to organization",
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    /**
     * Assign a role to an employee.
     */
    public void assignRoleToEmployee(UUID userId, UUID roleId, UUID organizationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                "USER_NOT_FOUND",
                "User not found with id: " + userId,
                HttpStatus.NOT_FOUND
            ));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(
                "ROLE_NOT_FOUND",
                "Role not found with id: " + roleId,
                HttpStatus.NOT_FOUND
            ));
        
        UserRoleId userRoleId = new UserRoleId(userId, roleId, organizationId);
        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setOrganization(user.getOrganization());
        
        userRoleRepository.save(userRole);
    }
    
    /**
     * Validate department change.
     */
    public void validateDepartmentChange(UUID orgId, UUID employeeId, UUID newDepartmentId) {
        Department department = departmentRepository
            .findByIdAndOrganizationId(newDepartmentId, orgId)
            .orElseThrow(() -> new DepartmentNotFoundException(newDepartmentId));
        
        if (!department.isActive()) {
            throw new BusinessException(
                "DEPARTMENT_INACTIVE",
                "Cannot assign employee to inactive department",
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    /**
     * Validate employee update.
     */
    public void validateEmployeeUpdate(User user) {
        // If email changed, check uniqueness
        // This is handled in the service layer where we can compare old vs new
        
        // Additional business rules can be added here
    }
    
    /**
     * Validate that employee can be deactivated.
     * For now, we just check if employee is active.
     * In the future, we can add checks for active assignments, projects, etc.
     */
    public void validateDeactivation(UUID employeeId) {
        User user = userRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException(
                "USER_NOT_FOUND",
                "User not found with id: " + employeeId,
                HttpStatus.NOT_FOUND
            ));
        
        if (!user.isActive()) {
            throw new BusinessException(
                "EMPLOYEE_ALREADY_INACTIVE",
                "Employee is already inactive",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // TODO: Add checks for active assignments, projects, tasks, etc.
        // if (projectRepository.hasActiveProjects(employeeId)) {
        //     throw new BusinessException(...);
        // }
    }
}

