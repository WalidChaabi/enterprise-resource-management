# 🎯 Enterprise Use Case: Employee Management
**Feature:** Create & Update Employee with Department Assignment  
**Priority:** HIGH - Foundational capability  
**Complexity:** Medium  
**Business Value:** Core HR functionality, enables all downstream features

---

## 📋 BUSINESS PROBLEM

### Current State
- Organizations can create users, but no structured employee management
- No department assignment workflow
- No employee lifecycle tracking
- No audit trail for employee data changes
- No role-based access control for employee management

### Business Need
Organizations need to:
1. **Onboard new employees** with proper department assignment
2. **Update employee information** (department transfers, role changes)
3. **Maintain data integrity** across organization boundaries
4. **Comply with audit requirements** (who changed what, when)
5. **Enforce access control** (only HR/managers can manage employees)

### Success Criteria
- ✅ HR can create employees with department assignment
- ✅ Managers can update employees in their department
- ✅ All changes are audited
- ✅ Multi-tenant isolation enforced
- ✅ Validation prevents data inconsistencies

---

## 👥 ACTORS & PERMISSIONS

### Roles & Permissions Matrix

| Role                   | Create Employee | Update Employee    | View All Employees | View Own Department | Delete Employee |
|----------------------  |---------------- |----------------    |------------------- |---------------------|---------------- |
| **HR_ADMIN**           | ✅             | ✅ (all)           | ✅                | ✅                  | ✅             |
| **DEPARTMENT_MANAGER** | ❌             | ✅ (own dept only) | ❌                | ✅                  | ❌             |
| **EMPLOYEE**           | ❌             | ❌                 | ❌                | ❌                  | ❌             |
| **ORG_ADMIN**          | ✅             | ✅ (all)           | ✅                | ✅                  | ✅             |

### Permission Details

**HR_ADMIN / ORG_ADMIN:**
- Full CRUD on all employees in their organization
- Can assign to any department
- Can change roles
- Can deactivate employees

**DEPARTMENT_MANAGER:**
- Can update employees in their managed department only
- Cannot change department assignment
- Cannot change roles
- Cannot create new employees
- Cannot delete employees

**Security Constraints:**
- Users can only manage employees within their own organization (multi-tenant isolation)
- Department managers can only access their assigned department
- All operations must be logged for audit

---

## 🔄 END-TO-END FLOW

### Use Case 1: Create Employee

```
1. HR Admin calls POST /api/v1/organizations/{orgId}/employees
2. Controller validates authentication & authorization
3. Controller validates request DTO
4. Service validates business rules:
   - Email uniqueness within organization
   - Department exists and belongs to organization
   - Department is active
   - User doesn't already exist
5. Service creates User entity
6. Service assigns to department
7. Service assigns default role (if specified)
8. Service logs audit event
9. Service returns created employee DTO
10. Controller returns 201 Created with Location header
```

### Use Case 2: Update Employee

```
1. Manager/HR calls PUT /api/v1/organizations/{orgId}/employees/{employeeId}
2. Controller validates authentication & authorization
3. Controller checks if employee belongs to organization
4. Controller checks if manager has permission (own dept or HR)
5. Service validates business rules:
   - Department change: new dept belongs to organization
   - Email change: new email is unique
   - Cannot deactivate if has active assignments
6. Service updates entity
7. Service logs audit event (what changed)
8. Service returns updated employee DTO
9. Controller returns 200 OK
```

---

## 🏗️ ARCHITECTURE & IMPLEMENTATION

### Package Structure

```
com.company.erm
├── domain/
│   ├── model/
│   │   └── Employee.java              // Domain entity (no JPA)
│   ├── repository/
│   │   └── EmployeeRepository.java    // Domain interface
│   └── service/
│       └── EmployeeDomainService.java  // Business rules
├── application/
│   ├── service/
│   │   └── EmployeeService.java       // Use case orchestration
│   └── dto/
│       ├── EmployeeCreateDTO.java
│       ├── EmployeeUpdateDTO.java
│       └── EmployeeResponseDTO.java
├── infrastructure/
│   ├── persistence/
│   │   ├── UserJpaEntity.java         // JPA entity
│   │   └── UserJpaRepository.java     // Spring Data
│   └── audit/
│       └── AuditLogService.java
└── controller/
    └── EmployeeController.java
```

---

## 📝 DTOs (Data Transfer Objects)

### EmployeeCreateDTO

```java
package com.company.erm.application.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for creating a new employee.
 * All fields are required for employee creation.
 */
public record EmployeeCreateDTO(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase, lowercase, digit, and special character"
    )
    String password,
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,
    
    @NotNull(message = "Department ID is required")
    UUID departmentId,
    
    @NotNull(message = "Default role ID is required")
    UUID defaultRoleId
) {
    // Validation can be enhanced with custom validators
}
```

### EmployeeUpdateDTO

```java
package com.company.erm.application.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.util.Optional;

/**
 * DTO for updating an existing employee.
 * All fields are optional - only provided fields will be updated.
 */
public record EmployeeUpdateDTO(
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    Optional<String> email,
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    Optional<String> firstName,
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    Optional<String> lastName,
    
    Optional<UUID> departmentId,
    
    Optional<Boolean> active
) {
    public EmployeeUpdateDTO {
        // Ensure at least one field is provided
        if (email.isEmpty() && firstName.isEmpty() && lastName.isEmpty() 
            && departmentId.isEmpty() && active.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }
}
```

### EmployeeResponseDTO

```java
package com.company.erm.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for employee response.
 * Contains all employee information including relationships.
 */
public record EmployeeResponseDTO(
    UUID id,
    UUID organizationId,
    String organizationName,
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String email,
    String firstName,
    String lastName,
    boolean active,
    List<RoleDTO> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String lastModifiedBy
) {
    public record RoleDTO(
        UUID id,
        String code,
        String name,
        String scope
    ) {}
}
```

---

## 🎮 CONTROLLER LAYER

### EmployeeController

```java
package com.company.erm.controller;

import com.company.erm.application.dto.EmployeeCreateDTO;
import com.company.erm.application.dto.EmployeeUpdateDTO;
import com.company.erm.application.dto.EmployeeResponseDTO;
import com.company.erm.application.service.EmployeeService;
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
 * REST controller for employee management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/employees")
@PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Create a new employee.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     * 
     * @param organizationId Organization ID (validated against authenticated user's org)
     * @param createDTO Employee creation data
     * @param authentication Current authenticated user
     * @return Created employee with 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @PathVariable UUID organizationId,
            @Valid @RequestBody EmployeeCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO created = employeeService.createEmployee(
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
     * Get employee by ID.
     * 
     * Permissions: 
     * - HR_ADMIN, ORG_ADMIN: any employee in organization
     * - DEPARTMENT_MANAGER: employees in their department only
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param authentication Current authenticated user
     * @return Employee details
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO employee = employeeService.getEmployee(
            organizationId, 
            employeeId, 
            currentUserEmail
        );
        
        return ResponseEntity.ok(employee);
    }

    /**
     * Update employee.
     * 
     * Permissions:
     * - HR_ADMIN, ORG_ADMIN: any employee in organization
     * - DEPARTMENT_MANAGER: employees in their department only (limited fields)
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param updateDTO Update data
     * @param authentication Current authenticated user
     * @return Updated employee
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        EmployeeResponseDTO updated = employeeService.updateEmployee(
            organizationId,
            employeeId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List employees with pagination and filtering.
     * 
     * Permissions:
     * - HR_ADMIN, ORG_ADMIN: all employees in organization
     * - DEPARTMENT_MANAGER: employees in their department only
     * 
     * @param organizationId Organization ID
     * @param departmentId Optional department filter
     * @param active Optional active status filter
     * @param pageable Pagination parameters
     * @param authentication Current authenticated user
     * @return Paginated list of employees
     */
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> listEmployees(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        Page<EmployeeResponseDTO> employees = employeeService.listEmployees(
            organizationId,
            departmentId,
            active,
            pageable,
            currentUserEmail
        );
        
        return ResponseEntity.ok(employees);
    }

    /**
     * Deactivate an employee.
     * 
     * Permissions: HR_ADMIN, ORG_ADMIN only
     * 
     * @param organizationId Organization ID
     * @param employeeId Employee ID
     * @param authentication Current authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ORG_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable UUID organizationId,
            @PathVariable UUID employeeId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        employeeService.deactivateEmployee(
            organizationId,
            employeeId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}
```

---

## 🔧 SERVICE LAYER

### EmployeeService (Application Service)

```java
package com.company.erm.application.service;

import com.company.erm.application.dto.*;
import com.company.erm.domain.model.Employee;
import com.company.erm.domain.repository.EmployeeRepository;
import com.company.erm.domain.service.EmployeeDomainService;
import com.company.erm.infrastructure.audit.AuditLogService;
import com.company.erm.infrastructure.persistence.UserJpaEntity;
import com.company.erm.infrastructure.persistence.UserJpaRepository;
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

    private final EmployeeRepository employeeRepository;
    private final UserJpaRepository userJpaRepository;
    private final EmployeeDomainService employeeDomainService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserJpaRepository userJpaRepository,
            EmployeeDomainService employeeDomainService,
            PasswordEncoder passwordEncoder,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.userJpaRepository = userJpaRepository;
        this.employeeDomainService = employeeDomainService;
        this.passwordEncoder = passwordEncoder;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.employeeMapper = employeeMapper;
    }

    /**
     * Create a new employee.
     * 
     * Business Rules:
     * - Email must be unique within organization
     * - Department must exist and be active
     * - Department must belong to organization
     * - Default role must exist and belong to organization
     * - Password must meet complexity requirements (validated in DTO)
     * 
     * Security:
     * - User must be HR_ADMIN or ORG_ADMIN
     * - User must belong to the organization
     * 
     * Audit:
     * - Log employee creation event
     */
    @Transactional
    public EmployeeResponseDTO createEmployee(
            UUID organizationId,
            EmployeeCreateDTO createDTO,
            String currentUserEmail) {
        
        // 1. Security check
        permissionChecker.requireRole(organizationId, currentUserEmail, 
            "HR_ADMIN", "ORG_ADMIN");
        
        // 2. Business validation (delegated to domain service)
        employeeDomainService.validateEmployeeCreation(
            organizationId,
            createDTO.email(),
            createDTO.departmentId(),
            createDTO.defaultRoleId()
        );
        
        // 3. Create user entity
        UserJpaEntity user = new UserJpaEntity();
        user.setId(UUID.randomUUID());
        user.setOrganizationId(organizationId);
        user.setEmail(createDTO.email());
        user.setPasswordHash(passwordEncoder.encode(createDTO.password()));
        user.setFirstName(createDTO.firstName());
        user.setLastName(createDTO.lastName());
        user.setDepartmentId(createDTO.departmentId());
        user.setActive(true);
        
        // 4. Save user
        UserJpaEntity savedUser = userJpaRepository.save(user);
        
        // 5. Assign default role (separate transaction handled by domain service)
        employeeDomainService.assignRoleToEmployee(
            savedUser.getId(),
            createDTO.defaultRoleId(),
            organizationId
        );
        
        // 6. Audit log
        auditLogService.logEmployeeCreated(
            organizationId,
            savedUser.getId(),
            createDTO.email(),
            currentUserEmail
        );
        
        // 7. Map to DTO
        return employeeMapper.toResponseDTO(savedUser);
    }

    /**
     * Update an existing employee.
     * 
     * Business Rules:
     * - Employee must exist and belong to organization
     * - If email changed, must be unique
     * - If department changed, new department must exist and be active
     * - Department managers can only update employees in their department
     * - Department managers cannot change department or roles
     * 
     * Security:
     * - HR_ADMIN/ORG_ADMIN: full update
     * - DEPARTMENT_MANAGER: limited update (no dept/role changes)
     */
    @Transactional
    public EmployeeResponseDTO updateEmployee(
            UUID organizationId,
            UUID employeeId,
            EmployeeUpdateDTO updateDTO,
            String currentUserEmail) {
        
        // 1. Load employee
        UserJpaEntity employee = userJpaRepository
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
                organizationId, currentUserEmail, employee.getDepartmentId());
            
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
        UUID oldDepartmentId = employee.getDepartmentId();
        boolean oldActive = employee.isActive();
        
        // 4. Apply updates
        updateDTO.email().ifPresent(employee::setEmail);
        updateDTO.firstName().ifPresent(employee::setFirstName);
        updateDTO.lastName().ifPresent(employee::setLastName);
        updateDTO.active().ifPresent(employee::setActive);
        
        // 5. Handle department change (if allowed)
        if (updateDTO.departmentId().isPresent() && isHrAdmin) {
            UUID newDepartmentId = updateDTO.departmentId().get();
            employeeDomainService.validateDepartmentChange(
                organizationId, employeeId, newDepartmentId);
            employee.setDepartmentId(newDepartmentId);
        }
        
        // 6. Business validation
        employeeDomainService.validateEmployeeUpdate(employee);
        
        // 7. Save
        UserJpaEntity updated = userJpaRepository.save(employee);
        
        // 8. Audit log
        auditLogService.logEmployeeUpdated(
            organizationId,
            employeeId,
            currentUserEmail,
            oldEmail, employee.getEmail(),
            oldDepartmentId, employee.getDepartmentId(),
            oldActive, employee.isActive()
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
        
        UserJpaEntity employee = userJpaRepository
            .findByIdAndOrganizationId(employeeId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // Permission check: managers can only see their department
        boolean isManager = permissionChecker.hasRole(
            organizationId, currentUserEmail, "DEPARTMENT_MANAGER");
        if (isManager) {
            permissionChecker.requireDepartmentAccess(
                organizationId, currentUserEmail, employee.getDepartmentId());
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
        
        Page<UserJpaEntity> employees = userJpaRepository
            .findByOrganizationIdAndFilters(organizationId, departmentId, active, pageable);
        
        return employees.map(employeeMapper::toResponseDTO);
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
        
        UserJpaEntity employee = userJpaRepository
            .findByIdAndOrganizationId(employeeId, organizationId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // Business rule: cannot deactivate if has active assignments
        employeeDomainService.validateDeactivation(employeeId);
        
        employee.setActive(false);
        userJpaRepository.save(employee);
        
        auditLogService.logEmployeeDeactivated(
            organizationId, employeeId, currentUserEmail);
    }
}
```

---

## 🛡️ SECURITY CONSTRAINTS

### Multi-Tenant Isolation

```java
/**
 * All queries must include organizationId filter.
 * Never allow cross-organization access.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    
    Optional<UserJpaEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    Page<UserJpaEntity> findByOrganizationIdAndFilters(
        UUID organizationId,
        UUID departmentId,
        Boolean active,
        Pageable pageable
    );
    
    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);
}
```

### Permission Checker Service

```java
package com.company.erm.security;

import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionChecker {
    
    private final UserRepository userRepository;
    
    public PermissionChecker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Require user to have one of the specified roles in the organization.
     */
    public void requireRole(UUID organizationId, String userEmail, String... roles) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        if (!user.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("User does not belong to organization");
        }
        
        boolean hasRole = user.getUserRoles().stream()
            .anyMatch(ur -> 
                ur.getOrganization().getId().equals(organizationId) &&
                java.util.Arrays.asList(roles).contains(ur.getRole().getCode())
            );
        
        if (!hasRole) {
            throw new AccessDeniedException(
                "User does not have required role: " + String.join(", ", roles));
        }
    }
    
    /**
     * Check if user has one of the specified roles.
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
     * Managers must manage the department.
     */
    public void requireDepartmentAccess(
            UUID organizationId, 
            String userEmail, 
            UUID departmentId) {
        // Implementation: check if user is manager of the department
        // or has HR_ADMIN/ORG_ADMIN role
    }
    
    /**
     * Get the department ID that a manager manages.
     */
    public UUID getUserDepartmentId(UUID organizationId, String userEmail) {
        // Implementation: return manager's department ID
    }
}
```

---

## 📊 AUDIT LOGGING

### AuditLogService

```java
package com.company.erm.infrastructure.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for audit logging.
 * 
 * In production, this should write to:
 * - Database audit table
 * - Log aggregation system (ELK, Splunk)
 * - Event streaming (Kafka) for real-time monitoring
 */
@Service
public class AuditLogService {
    
    private static final Logger auditLogger = 
        LoggerFactory.getLogger("AUDIT");
    
    /**
     * Log employee creation.
     */
    public void logEmployeeCreated(
            UUID organizationId,
            UUID employeeId,
            String employeeEmail,
            String createdBy) {
        
        AuditEvent event = AuditEvent.builder()
            .eventType("EMPLOYEE_CREATED")
            .organizationId(organizationId)
            .entityType("EMPLOYEE")
            .entityId(employeeId)
            .actor(createdBy)
            .timestamp(LocalDateTime.now())
            .details(java.util.Map.of(
                "employeeEmail", employeeEmail,
                "action", "CREATE"
            ))
            .build();
        
        logEvent(event);
    }
    
    /**
     * Log employee update with change tracking.
     */
    public void logEmployeeUpdated(
            UUID organizationId,
            UUID employeeId,
            String modifiedBy,
            String oldEmail, String newEmail,
            UUID oldDepartmentId, UUID newDepartmentId,
            boolean oldActive, boolean newActive) {
        
        java.util.Map<String, Object> changes = new java.util.HashMap<>();
        
        if (!oldEmail.equals(newEmail)) {
            changes.put("email", java.util.Map.of("old", oldEmail, "new", newEmail));
        }
        if (!oldDepartmentId.equals(newDepartmentId)) {
            changes.put("departmentId", 
                java.util.Map.of("old", oldDepartmentId, "new", newDepartmentId));
        }
        if (oldActive != newActive) {
            changes.put("active", java.util.Map.of("old", oldActive, "new", newActive));
        }
        
        AuditEvent event = AuditEvent.builder()
            .eventType("EMPLOYEE_UPDATED")
            .organizationId(organizationId)
            .entityType("EMPLOYEE")
            .entityId(employeeId)
            .actor(modifiedBy)
            .timestamp(LocalDateTime.now())
            .details(changes)
            .build();
        
        logEvent(event);
    }
    
    /**
     * Log employee deactivation.
     */
    public void logEmployeeDeactivated(
            UUID organizationId,
            UUID employeeId,
            String deactivatedBy) {
        
        AuditEvent event = AuditEvent.builder()
            .eventType("EMPLOYEE_DEACTIVATED")
            .organizationId(organizationId)
            .entityType("EMPLOYEE")
            .entityId(employeeId)
            .actor(deactivatedBy)
            .timestamp(LocalDateTime.now())
            .details(java.util.Map.of("action", "DEACTIVATE"))
            .build();
        
        logEvent(event);
    }
    
    private void logEvent(AuditEvent event) {
        // Log to structured logger (JSON format for parsing)
        auditLogger.info("AUDIT_EVENT: {}", toJson(event));
        
        // In production, also persist to database
        // auditRepository.save(toAuditEntity(event));
    }
    
    private String toJson(AuditEvent event) {
        // Use Jackson or similar to serialize
        return "{...}";
    }
}

/**
 * Audit event data structure.
 */
public record AuditEvent(
    String eventType,
    UUID organizationId,
    String entityType,
    UUID entityId,
    String actor,
    LocalDateTime timestamp,
    java.util.Map<String, Object> details
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        // Builder implementation
    }
}
```

### Database Audit Table (Optional)

```sql
-- Migration: V4__create_audit_log_table.sql
CREATE TABLE IF NOT EXISTS erm.audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES erm.organizations(id),
    event_type      VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    actor           VARCHAR(255) NOT NULL,
    timestamp       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details         JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    
    INDEX idx_audit_org_time (organization_id, timestamp),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_actor (actor, timestamp)
);
```

---

## 🔌 API ENDPOINTS

### RESTful API Design

| Method | Endpoint                                       | Description         | Auth Required                     |
|--------|------------------------------------------------|---------------------|-----------------------------------|
| POST   | `/api/v1/organizations/{orgId}/employees`      | Create employee     | HR_ADMIN, ORG_ADMIN               |
| GET    | `/api/v1/organizations/{orgId}/employees/{id}` | Get employee        | HR_ADMIN, ORG_ADMIN, DEPT_MANAGER |
| PUT    | `/api/v1/organizations/{orgId}/employees/{id}` | Update employee     | HR_ADMIN, ORG_ADMIN, DEPT_MANAGER |
| DELETE | `/api/v1/organizations/{orgId}/employees/{id}` | Deactivate employee | HR_ADMIN, ORG_ADMIN               |
| GET    | `/api/v1/organizations/{orgId}/employees`      | List employees      | HR_ADMIN, ORG_ADMIN, DEPT_MANAGER |

### Request/Response Examples

**Create Employee:**
```http
POST /api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "john.doe@company.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "departmentId": "660e8400-e29b-41d4-a716-446655440001",
  "defaultRoleId": "770e8400-e29b-41d4-a716-446655440002"
}

Response: 201 Created
Location: /api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/880e8400-e29b-41d4-a716-446655440003
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "departmentId": "660e8400-e29b-41d4-a716-446655440001",
  "departmentName": "Engineering",
  "departmentCode": "ENG",
  "email": "john.doe@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "roles": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "code": "EMPLOYEE",
      "name": "Employee",
      "scope": "ORG"
    }
  ],
  "createdAt": "2026-01-06T10:00:00",
  "updatedAt": "2026-01-06T10:00:00",
  "createdBy": "hr.admin@company.com",
  "lastModifiedBy": "hr.admin@company.com"
}
```

**Update Employee:**
```http
PUT /api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/880e8400-e29b-41d4-a716-446655440003
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Jonathan",
  "departmentId": "660e8400-e29b-41d4-a716-446655440002"
}

Response: 200 OK
{
  // Updated employee response
}
```

**List Employees (with pagination):**
```http
GET /api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?departmentId=660e8400-e29b-41d4-a716-446655440001&active=true&page=0&size=20&sort=lastName,asc
Authorization: Bearer <token>

Response: 200 OK
{
  "content": [
    // EmployeeResponseDTO array
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

## ⚠️ EDGE CASES & FAILURE SCENARIOS

### 1. **Email Already Exists**

**Scenario:** Create employee with email that already exists in organization

**Handling:**
```java
// In EmployeeDomainService
public void validateEmployeeCreation(...) {
    if (userRepository.existsByEmailAndOrganizationId(email, organizationId)) {
        throw new BusinessException(
            "EMAIL_ALREADY_EXISTS",
            "Email already exists in organization",
            HttpStatus.CONFLICT
        );
    }
}
```

**Response:** `409 Conflict` with error code `EMAIL_ALREADY_EXISTS`

---

### 2. **Department Does Not Exist**

**Scenario:** Create/update employee with non-existent department

**Handling:**
```java
public void validateDepartmentChange(UUID orgId, UUID employeeId, UUID deptId) {
    Department dept = departmentRepository
        .findByIdAndOrganizationId(deptId, orgId)
        .orElseThrow(() -> new DepartmentNotFoundException(deptId));
    
    if (!dept.isActive()) {
        throw new BusinessException(
            "DEPARTMENT_INACTIVE",
            "Cannot assign employee to inactive department",
            HttpStatus.BAD_REQUEST
        );
    }
}
```

**Response:** `404 Not Found` or `400 Bad Request` with specific error code

---

### 3. **Concurrent Update**

**Scenario:** Two users update same employee simultaneously

**Handling:**
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public EmployeeResponseDTO updateEmployee(...) {
    // Use optimistic locking
    UserJpaEntity employee = userJpaRepository
        .findByIdAndOrganizationId(employeeId, organizationId)
        .orElseThrow(...);
    
    // Check version/timestamp
    if (employee.getUpdatedAt().isAfter(updateDTO.lastKnownUpdate())) {
        throw new OptimisticLockingException("Employee was modified by another user");
    }
    
    // Update...
}
```

**Response:** `409 Conflict` with `OPTIMISTIC_LOCK_ERROR`

---

### 4. **Manager Tries to Change Department**

**Scenario:** Department manager attempts to change employee's department

**Handling:**
```java
// Already handled in updateEmployee method
if (isManager && updateDTO.departmentId().isPresent()) {
    throw new InsufficientPermissionException(
        "Department managers cannot change employee department"
    );
}
```

**Response:** `403 Forbidden` with `INSUFFICIENT_PERMISSION`

---

### 5. **Deactivate Employee with Active Assignments**

**Scenario:** Try to deactivate employee who has active projects/tasks

**Handling:**
```java
public void validateDeactivation(UUID employeeId) {
    // Check for active assignments
    if (projectRepository.hasActiveProjects(employeeId)) {
        throw new BusinessException(
            "EMPLOYEE_HAS_ACTIVE_ASSIGNMENTS",
            "Cannot deactivate employee with active project assignments",
            HttpStatus.CONFLICT
        );
    }
    
    if (taskRepository.hasActiveTasks(employeeId)) {
        throw new BusinessException(
            "EMPLOYEE_HAS_ACTIVE_TASKS",
            "Cannot deactivate employee with active tasks",
            HttpStatus.CONFLICT
        );
    }
}
```

**Response:** `409 Conflict` with specific error code

---

### 6. **Cross-Organization Access Attempt**

**Scenario:** User from Org A tries to access employee from Org B

**Handling:**
```java
// Enforced at repository level
public Optional<UserJpaEntity> findByIdAndOrganizationId(UUID id, UUID orgId) {
    // Always filter by organizationId
}

// Additional check in service
if (!employee.getOrganizationId().equals(organizationId)) {
    throw new AccessDeniedException("Employee does not belong to organization");
}
```

**Response:** `403 Forbidden` or `404 Not Found` (hide existence)

---

### 7. **Invalid Password Format**

**Scenario:** Password doesn't meet complexity requirements

**Handling:**
```java
// Validated at DTO level with @Pattern
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least one uppercase, lowercase, digit, and special character"
)
String password;
```

**Response:** `400 Bad Request` with validation errors

---

### 8. **Database Transaction Failure**

**Scenario:** Database error during employee creation

**Handling:**
```java
@Transactional(
    rollbackFor = Exception.class,
    isolation = Isolation.READ_COMMITTED
)
public EmployeeResponseDTO createEmployee(...) {
    try {
        // Operations
    } catch (DataIntegrityViolationException e) {
        // Handle constraint violations
        throw new BusinessException("DATA_INTEGRITY_ERROR", 
            "Database constraint violation", HttpStatus.CONFLICT);
    } catch (Exception e) {
        // Log and rethrow
        logger.error("Failed to create employee", e);
        throw new BusinessException("INTERNAL_ERROR",
            "Failed to create employee", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Response:** `500 Internal Server Error` or `409 Conflict` depending on error type

---

## ✅ VALIDATION RULES

### Business Rules Summary

1. **Email Uniqueness:** Must be unique within organization
2. **Department Validation:** Must exist, be active, and belong to organization
3. **Role Validation:** Must exist and belong to organization
4. **Password Complexity:** Min 8 chars, uppercase, lowercase, digit, special char
5. **Permission Checks:** Role-based access enforced at service layer
6. **Multi-Tenant Isolation:** All operations scoped to organization
7. **Concurrent Updates:** Optimistic locking for data consistency
8. **Deactivation Rules:** Cannot deactivate if has active assignments

---

## 🚀 IMPLEMENTATION CHECKLIST

### Phase 1: Core Implementation (Week 1)
- [ ] Create DTOs (Create, Update, Response)
- [ ] Implement EmployeeService with basic CRUD
- [ ] Create EmployeeController with endpoints
- [ ] Add PermissionChecker service
- [ ] Implement basic validation

### Phase 2: Security & Permissions (Week 1-2)
- [ ] Implement role-based access control
- [ ] Add department manager restrictions
- [ ] Enforce multi-tenant isolation
- [ ] Add security tests

### Phase 3: Audit & Logging (Week 2)
- [ ] Implement AuditLogService
- [ ] Add audit logging to all operations
- [ ] Create audit log table (optional)
- [ ] Add audit log queries

### Phase 4: Edge Cases & Testing (Week 2-3)
- [ ] Handle all edge cases
- [ ] Write unit tests (80%+ coverage)
- [ ] Write integration tests
- [ ] Performance testing

### Phase 5: Documentation & Polish (Week 3)
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Update README
- [ ] Code review
- [ ] Performance optimization

---

## 📈 SUCCESS METRICS

- ✅ All CRUD operations work correctly
- ✅ Security enforced (no unauthorized access)
- ✅ Audit trail complete (all changes logged)
- ✅ Performance acceptable (< 200ms for list, < 100ms for get)
- ✅ Test coverage > 80%
- ✅ Zero critical security vulnerabilities
- ✅ API documentation complete

---

**This use case demonstrates:**
- ✅ Clean architecture (domain/application/infrastructure separation)
- ✅ Enterprise security (RBAC, multi-tenant isolation)
- ✅ Comprehensive audit logging
- ✅ Proper transaction management
- ✅ Edge case handling
- ✅ RESTful API design
- ✅ Production-ready patterns

**Next Steps After This:**
1. Leave Request Management (workflow, approvals)
2. Performance Review System
3. Department Management
4. Role & Permission Management

