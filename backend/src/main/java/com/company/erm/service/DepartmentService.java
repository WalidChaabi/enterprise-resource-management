package com.company.erm.service;

import com.company.erm.dto.DepartmentCreateDTO;
import com.company.erm.dto.DepartmentUpdateDTO;
import com.company.erm.dto.DepartmentResponseDTO;
import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.exception.DepartmentNotFoundException;
import com.company.erm.mapper.DepartmentMapper;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.security.PermissionChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for department management use cases.
 */
@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentDomainService departmentDomainService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            OrganizationRepository organizationRepository,
            DepartmentDomainService departmentDomainService,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.departmentDomainService = departmentDomainService;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Create a new department.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Transactional
    public DepartmentResponseDTO createDepartment(
            UUID organizationId,
            DepartmentCreateDTO createDTO,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "HR_ADMIN", "ORG_ADMIN");
        
        // Load organization
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        
        // Business validation
        departmentDomainService.validateDepartmentCreation(
            organizationId,
            createDTO.code(),
            createDTO.parentDepartmentId()
        );
        
        // Create department entity
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOrganization(organization);
        department.setName(createDTO.name());
        department.setCode(createDTO.code().toUpperCase()); // Normalize to uppercase
        department.setActive(true);
        
        // Set parent if provided
        if (createDTO.parentDepartmentId() != null) {
            Department parent = departmentRepository
                .findByIdAndOrganizationId(createDTO.parentDepartmentId(), organizationId)
                .orElseThrow(() -> new DepartmentNotFoundException(createDTO.parentDepartmentId()));
            department.setParent(parent);
        }
        
        // Save
        Department saved = departmentRepository.save(department);
        
        // Audit log
        auditLogService.logDepartmentCreated(
            organizationId,
            saved.getId(),
            saved.getCode(),
            currentUserEmail
        );
        
        // Map to DTO
        return departmentMapper.toResponseDTO(saved);
    }

    /**
     * Update an existing department.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Transactional
    public DepartmentResponseDTO updateDepartment(
            UUID organizationId,
            UUID departmentId,
            DepartmentUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // Security check
        permissionChecker.requireRole(organizationId, currentUserEmail,
            "HR_ADMIN", "ORG_ADMIN");
        
        // Load department
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        // Track changes for audit
        String oldCode = department.getCode();
        UUID oldParentId = department.getParent() != null ? department.getParent().getId() : null;
        boolean oldActive = department.isActive();
        
        // Apply updates
        updateDTO.name().ifPresent(department::setName);
        updateDTO.code().ifPresent(code -> department.setCode(code.toUpperCase()));
        updateDTO.active().ifPresent(active -> department.setActive(active));
        
        // Handle parent change
        if (updateDTO.parentDepartmentId().isPresent()) {
            UUID newParentId = updateDTO.parentDepartmentId().get();
            
            // If null, remove parent
            if (newParentId == null) {
                department.setParent(null);
            } else {
                // Validate parent change
                departmentDomainService.validateDepartmentUpdate(
                    organizationId, departmentId, 
                    updateDTO.code().orElse(null), 
                    newParentId
                );
                
                Department newParent = departmentRepository
                    .findByIdAndOrganizationId(newParentId, organizationId)
                    .orElseThrow(() -> new DepartmentNotFoundException(newParentId));
                department.setParent(newParent);
            }
        } else if (updateDTO.code().isPresent()) {
            // Validate code change only
            departmentDomainService.validateDepartmentUpdate(
                organizationId, departmentId,
                updateDTO.code().orElse(null),
                null
            );
        }
        
        // Save
        Department updated = departmentRepository.save(department);
        
        // Audit log
        UUID newParentId = updated.getParent() != null ? updated.getParent().getId() : null;
        auditLogService.logDepartmentUpdated(
            organizationId,
            departmentId,
            currentUserEmail,
            oldCode, updated.getCode(),
            oldParentId, newParentId,
            oldActive, updated.isActive()
        );
        
        // Map to DTO
        return departmentMapper.toResponseDTO(updated);
    }

    /**
     * Get department by ID.
     */
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartment(
            UUID organizationId,
            UUID departmentId,
            String currentUserEmail) {
        
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        return departmentMapper.toResponseDTO(department);
    }

    /**
     * List departments with filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<DepartmentResponseDTO> listDepartments(
            UUID organizationId,
            UUID parentId,
            Boolean active,
            Pageable pageable,
            String currentUserEmail) {
        
        Page<Department> departments = departmentRepository
            .findByOrganizationIdAndFilters(organizationId, active, parentId, pageable);
        
        return departments.map(departmentMapper::toResponseDTO);
    }

    /**
     * Deactivate a department (soft delete).
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     */
    @Transactional
    public void deactivateDepartment(
            UUID organizationId,
            UUID departmentId,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail,
            "HR_ADMIN", "ORG_ADMIN");
        
        Department department = departmentRepository
            .findByIdAndOrganizationId(departmentId, organizationId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        // Business rule: cannot deactivate if has active children or employees
        departmentDomainService.validateDeactivation(organizationId, departmentId);
        
        department.setActive(false);
        departmentRepository.save(department);
        
        auditLogService.logDepartmentDeactivated(
            organizationId, departmentId, currentUserEmail);
    }
}
