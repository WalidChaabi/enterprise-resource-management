package com.company.erm.service;

import com.company.erm.entity.Department;
import com.company.erm.exception.BusinessException;
import com.company.erm.exception.DepartmentAlreadyExistsException;
import com.company.erm.exception.DepartmentNotFoundException;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain service for department business rules and validations.
 */
@Service
@Transactional
public class DepartmentDomainService {
    
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    
    public DepartmentDomainService(
            DepartmentRepository departmentRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Validate department creation business rules.
     */
    public void validateDepartmentCreation(
            UUID organizationId,
            String code,
            UUID parentDepartmentId) {
        
        // Check code uniqueness within organization
        organizationRepository.findById(organizationId)
            .ifPresent(org -> {
                if (departmentRepository.findByOrganizationAndCode(org, code).isPresent()) {
                    throw new DepartmentAlreadyExistsException(code);
                }
            });
        
        // Validate parent department if provided
        if (parentDepartmentId != null) {
            Department parent = departmentRepository
                .findByIdAndOrganizationId(parentDepartmentId, organizationId)
                .orElseThrow(() -> new DepartmentNotFoundException(parentDepartmentId));
            
            if (!parent.isActive()) {
                throw new BusinessException(
                    "PARENT_DEPARTMENT_INACTIVE",
                    "Cannot create department under inactive parent department",
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Prevent circular references (parent cannot be a child of this department)
            // This would be checked at update time if we allow changing parent
        }
    }
    
    /**
     * Validate department update.
     */
    public void validateDepartmentUpdate(
            UUID organizationId,
            UUID departmentId,
            String newCode,
            UUID newParentId) {
        
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        // If code changed, check uniqueness
        if (newCode != null && !newCode.equals(department.getCode())) {
            organizationRepository.findById(organizationId)
                .ifPresent(org -> {
                    if (departmentRepository.findByOrganizationAndCode(org, newCode).isPresent()) {
                        throw new DepartmentAlreadyExistsException(newCode);
                    }
                });
        }
        
        // Validate parent department change
        if (newParentId != null) {
            // Cannot set itself as parent
            if (newParentId.equals(departmentId)) {
                throw new BusinessException(
                    "CIRCULAR_REFERENCE",
                    "Department cannot be its own parent",
                    HttpStatus.BAD_REQUEST
                );
            }
            
            Department newParent = departmentRepository
                .findByIdAndOrganizationId(newParentId, organizationId)
                .orElseThrow(() -> new DepartmentNotFoundException(newParentId));
            
            if (!newParent.isActive()) {
                throw new BusinessException(
                    "PARENT_DEPARTMENT_INACTIVE",
                    "Cannot set inactive department as parent",
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Check for circular reference: new parent cannot be a descendant of this department
            checkCircularReference(department, newParent);
        }
    }
    
    /**
     * Validate that a department can be deactivated.
     */
    public void validateDeactivation(UUID organizationId, UUID departmentId) {
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        if (!department.isActive()) {
            throw new BusinessException(
                "DEPARTMENT_ALREADY_INACTIVE",
                "Department is already inactive",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Check if department has active sub-departments
        long activeChildren = departmentRepository.findByOrganizationId(organizationId).stream()
            .filter(d -> d.getParent() != null && d.getParent().getId().equals(departmentId))
            .filter(Department::isActive)
            .count();
        
        if (activeChildren > 0) {
            throw new BusinessException(
                "DEPARTMENT_HAS_ACTIVE_CHILDREN",
                "Cannot deactivate department with active sub-departments",
                HttpStatus.CONFLICT
            );
        }
        
        // Check if department has active employees
        long activeEmployees = userRepository.findAll().stream()
            .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(departmentId))
            .filter(u -> u.getOrganization().getId().equals(organizationId))
            .filter(u -> u.isActive())
            .count();
        
        if (activeEmployees > 0) {
            throw new BusinessException(
                "DEPARTMENT_HAS_ACTIVE_EMPLOYEES",
                "Cannot deactivate department with active employees",
                HttpStatus.CONFLICT
            );
        }
    }
    
    /**
     * Check for circular reference in department hierarchy.
     */
    private void checkCircularReference(Department department, Department potentialParent) {
        Department current = potentialParent.getParent();
        int depth = 0;
        final int MAX_DEPTH = 100; // Prevent infinite loops
        
        while (current != null && depth < MAX_DEPTH) {
            if (current.getId().equals(department.getId())) {
                throw new BusinessException(
                    "CIRCULAR_REFERENCE",
                    "Cannot create circular reference in department hierarchy",
                    HttpStatus.BAD_REQUEST
                );
            }
            current = current.getParent();
            depth++;
        }
    }
}
