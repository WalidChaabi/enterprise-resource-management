package com.company.erm.service;

import com.company.erm.dto.EmployeeCreateDTO;
import com.company.erm.dto.EmployeeUpdateDTO;
import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.exception.EmployeeNotFoundException;
import com.company.erm.exception.InsufficientPermissionException;
import com.company.erm.mapper.EmployeeMapper;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for employee management use cases.
 * 
 * Responsibilities:
 * - Orchestrate use cases
 * - Coordinate between domain and infrastructure
 * - Handle transactions
 * - Enforce security
 * - Log audit events
 */
@Service
@Transactional
public class EmployeeService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeDomainService employeeDomainService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final EmployeeMapper employeeMapper;
    private final EntitlementService entitlementService;

    public EmployeeService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            EmployeeDomainService employeeDomainService,
            PasswordEncoder passwordEncoder,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            EmployeeMapper employeeMapper,
            EntitlementService entitlementService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.employeeDomainService = employeeDomainService;
        this.passwordEncoder = passwordEncoder;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.employeeMapper = employeeMapper;
        this.entitlementService = entitlementService;
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public EmployeeResponseDTO createEmployee(
            UUID organizationId,
            EmployeeCreateDTO createDTO,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "HR_ADMIN", "ORG_ADMIN");
        
        // 2. Load organization
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        entitlementService.requireSeatAvailable(organization);
        
        // 3. Business validation (delegated to domain service)
        employeeDomainService.validateEmployeeCreation(
            organizationId,
            createDTO.email(),
            createDTO.departmentId(),
            createDTO.defaultRoleId()
        );
        
        // 4. Load department
        Department department = departmentRepository.findById(createDTO.departmentId())
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        
        // 5. Create user entity
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(organization);
        user.setEmail(createDTO.email());
        user.setPasswordHash(passwordEncoder.encode(createDTO.password()));
        user.setFirstName(createDTO.firstName());
        user.setLastName(createDTO.lastName());
        user.setDepartment(department);
        user.setActive(true);
        
        // 6. Save user
        User savedUser = userRepository.save(user);
        
        // 7. Assign default role (separate transaction handled by domain service)
        employeeDomainService.assignRoleToEmployee(
            savedUser.getId(),
            createDTO.defaultRoleId(),
            organizationId
        );
        
        // 8. Audit log
        auditLogService.logEmployeeCreated(
            organizationId,
            savedUser.getId(),
            createDTO.email(),
            currentUserEmail
        );
        
        // 9. Map to DTO
        return employeeMapper.toResponseDTO(savedUser);
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public EmployeeResponseDTO updateEmployee(
            UUID organizationId,
            UUID employeeId,
            EmployeeUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // 1. Load employee
        User employee = userRepository
            .findByIdAndOrganizationId(employeeId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // 2. Security check
        boolean isManager = permissionChecker.hasRole(
            organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        boolean isHrAdmin = permissionChecker.hasRole(
            organizationId, currentUserEmail, "HR_ADMIN", "ORG_ADMIN");
        
        if (isManager && !isHrAdmin) {
            // Manager can only update employees in their department
            permissionChecker.requireDepartmentAccess(
                organizationId, currentUserEmail, employee.getDepartment().getId());
            
            // Manager cannot change department or roles
            if (updateDTO.departmentId().isPresent()) {
                throw new InsufficientPermissionException(
                    "Department managers cannot change employee department");
            }
        } else {
            permissionChecker.requireRole(organizationId, currentUserEmail,
                "HR_ADMIN", "ORG_ADMIN");
        }
        
        // 3. Track changes for audit
        String oldEmail = employee.getEmail();
        UUID oldDepartmentId = employee.getDepartment() != null ? 
            employee.getDepartment().getId() : null;
        boolean oldActive = employee.isActive();
        
        // 4. Apply updates
        updateDTO.email().ifPresent(employee::setEmail);
        updateDTO.firstName().ifPresent(employee::setFirstName);
        updateDTO.lastName().ifPresent(employee::setLastName);
        updateDTO.active().ifPresent(active -> employee.setActive(active));
        
        // 5. Handle department change (if allowed)
        if (updateDTO.departmentId().isPresent() && isHrAdmin) {
            UUID newDepartmentId = updateDTO.departmentId().get();
            employeeDomainService.validateDepartmentChange(
                organizationId, employeeId, newDepartmentId);
            Department newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            employee.setDepartment(newDepartment);
        }
        
        // 6. Business validation
        employeeDomainService.validateEmployeeUpdate(employee);
        
        // 7. Save
        User updated = userRepository.save(employee);
        
        // 8. Audit log
        UUID newDepartmentId = updated.getDepartment() != null ? 
            updated.getDepartment().getId() : null;
        auditLogService.logEmployeeUpdated(
            organizationId,
            employeeId,
            currentUserEmail,
            oldEmail, updated.getEmail(),
            oldDepartmentId, newDepartmentId,
            oldActive, updated.isActive()
        );
        
        // 9. Map to DTO
        return employeeMapper.toResponseDTO(updated);
    }

    /**
     * Get employee by ID with permission checks.
     */
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployee(
            UUID organizationId,
            UUID employeeId,
            String currentUserEmail) {
        
        User employee = userRepository
            .findByIdAndOrganizationId(employeeId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // Permission check: managers can only see their department
        boolean isManager = permissionChecker.hasRole(
            organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        if (isManager && employee.getDepartment() != null) {
            permissionChecker.requireDepartmentAccess(
                organizationId, currentUserEmail, employee.getDepartment().getId());
        }
        
        return employeeMapper.toResponseDTO(employee);
    }

    /**
     * List employees with filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> listEmployees(
            UUID organizationId,
            UUID departmentId,
            Boolean active,
            Pageable pageable,
            String currentUserEmail) {
        
        // Permission check: managers can only see their department
        boolean isManager = permissionChecker.hasRole(
            organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        
        if (isManager) {
            // Force department filter to manager's department
            UUID managerDepartmentId = permissionChecker
                .getUserDepartmentId(organizationId, currentUserEmail);
            departmentId = managerDepartmentId;
        }
        
        Page<User> employees = userRepository
            .findByOrganizationIdAndFilters(organizationId, departmentId, active, pageable);
        
        return employeeMapper.toResponsePage(employees, organizationId);
    }

    /**
     * Deactivate an employee (soft delete).
     */
    @Transactional
    public void deactivateEmployee(
            UUID organizationId,
            UUID employeeId,
            String currentUserEmail) {
        
        permissionChecker.requireRole(organizationId, currentUserEmail,
            "HR_ADMIN", "ORG_ADMIN");
        
        User employee = userRepository
            .findByIdAndOrganizationId(employeeId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // Business rule: cannot deactivate if has active assignments
        employeeDomainService.validateDeactivation(employeeId);
        
        employee.setActive(false);
        userRepository.save(employee);
        
        auditLogService.logEmployeeDeactivated(
            organizationId, employeeId, currentUserEmail);
    }
}

