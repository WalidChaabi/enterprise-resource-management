package com.company.erm.service;

import com.company.erm.dto.EmployeeCreateDTO;
import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.dto.EmployeeUpdateDTO;
import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.exception.EmployeeNotFoundException;
import com.company.erm.exception.InsufficientPermissionException;
import com.company.erm.mapper.EmployeeMapper;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * 
 * Tests critical employee management paths:
 * - Employee creation with permission checks
 * - Employee update with security enforcement
 * - Permission validation
 * - Multi-tenant isolation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Tests")
class EmployeeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeDomainService employeeDomainService;

    @Mock
    private PermissionChecker permissionChecker;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private EntitlementService entitlementService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private PasswordEncoder realPasswordEncoder;
    private Organization organization;
    private Department department;
    private Role defaultRole;
    private User currentUser;
    private User employee;
    private UUID organizationId;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        realPasswordEncoder = new BCryptPasswordEncoder();
        organization = TestDataFactory.createOrganization();
        organizationId = organization.getId();
        department = TestDataFactory.createDepartment(organization);
        departmentId = department.getId();
        defaultRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleId = defaultRole.getId();
        currentUser = TestDataFactory.createUser(organization, department, "admin@example.com", "password", realPasswordEncoder);
        employee = TestDataFactory.createUser(organization, department, "employee@example.com", "password", realPasswordEncoder);
    }

    @Test
    @DisplayName("Should create employee when user has HR_ADMIN role")
    void createEmployee_shouldSucceed_whenUserHasHrAdminRole() {
        // Given
        EmployeeCreateDTO createDTO = new EmployeeCreateDTO(
                "newemployee@example.com",
                "Password123!",
                "John",
                "Doe",
                departmentId,
                roleId
        );

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        doNothing().when(permissionChecker).requireRole(eq(organizationId), eq("admin@example.com"), any(String[].class));
        doNothing().when(entitlementService).requireSeatAvailable(any(Organization.class));
        doNothing().when(employeeDomainService).validateEmployeeCreation(any(), any(), any(), any());
        doNothing().when(employeeDomainService).assignRoleToEmployee(any(), any(), any());
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> realPasswordEncoder.encode(invocation.getArgument(0)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toResponseDTO(any(User.class))).thenReturn(
                new EmployeeResponseDTO(
                        UUID.randomUUID(),
                        organizationId,
                        organization.getName(),
                        departmentId,
                        department.getName(),
                        department.getCode(),
                        "newemployee@example.com",
                        "John",
                        "Doe",
                        true,
                        List.of(),
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now(),
                        null,
                        null
                )
        );

        // When
        EmployeeResponseDTO result = employeeService.createEmployee(organizationId, createDTO, "admin@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("newemployee@example.com");
        verify(permissionChecker).requireRole(organizationId, "admin@example.com", "HR_ADMIN", "ORG_ADMIN");
        verify(userRepository).save(any(User.class));
        verify(employeeDomainService).assignRoleToEmployee(any(), any(), eq(organizationId));
        verify(auditLogService).logEmployeeCreated(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when user lacks permission to create employee")
    void createEmployee_shouldThrowException_whenUserLacksPermission() {
        // Given
        EmployeeCreateDTO createDTO = new EmployeeCreateDTO(
                "newemployee@example.com",
                "Password123!",
                "John",
                "Doe",
                departmentId,
                roleId
        );

        doThrow(new InsufficientPermissionException("Insufficient permissions"))
                .when(permissionChecker).requireRole(eq(organizationId), eq("user@example.com"), eq("HR_ADMIN"), eq("ORG_ADMIN"));

        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(organizationId, createDTO, "user@example.com"))
                .isInstanceOf(InsufficientPermissionException.class);

        verify(permissionChecker).requireRole(organizationId, "user@example.com", "HR_ADMIN", "ORG_ADMIN");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get employee when user has access")
    void getEmployee_shouldReturnEmployee_whenUserHasAccess() {
        // Given
        UUID employeeId = employee.getId();
        when(userRepository.findByIdAndOrganizationId(employeeId, organizationId)).thenReturn(Optional.of(employee));
        when(permissionChecker.hasRole(organizationId, "admin@example.com", "DEPARTMENT_MANAGER")).thenReturn(false);
        when(employeeMapper.toResponseDTO(employee)).thenReturn(
                new EmployeeResponseDTO(
                        employeeId,
                        organizationId,
                        organization.getName(),
                        departmentId,
                        department.getName(),
                        department.getCode(),
                        "employee@example.com",
                        "Test",
                        "User",
                        true,
                        List.of(),
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now(),
                        null,
                        null
                )
        );

        // When
        EmployeeResponseDTO result = employeeService.getEmployee(organizationId, employeeId, "admin@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("employee@example.com");
        verify(userRepository).findByIdAndOrganizationId(employeeId, organizationId);
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void getEmployee_shouldThrowException_whenEmployeeNotFound() {
        // Given
        UUID employeeId = UUID.randomUUID();
        when(userRepository.findByIdAndOrganizationId(employeeId, organizationId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.getEmployee(organizationId, employeeId, "admin@example.com"))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(userRepository).findByIdAndOrganizationId(employeeId, organizationId);
    }

    @Test
    @DisplayName("Should update employee when user has permission")
    void updateEmployee_shouldSucceed_whenUserHasPermission() {
        // Given
        UUID employeeId = employee.getId();
        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO(
                Optional.of("updated@example.com"),
                Optional.of("Updated"),
                Optional.of("Name"),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findByIdAndOrganizationId(employeeId, organizationId)).thenReturn(Optional.of(employee));
        when(permissionChecker.hasRole(organizationId, "admin@example.com", "DEPARTMENT_MANAGER")).thenReturn(false);
        when(permissionChecker.hasRole(organizationId, "admin@example.com", "HR_ADMIN", "ORG_ADMIN")).thenReturn(true);
        doNothing().when(permissionChecker).requireRole(eq(organizationId), eq("admin@example.com"), any(String[].class));
        doNothing().when(employeeDomainService).validateEmployeeUpdate(any());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toResponseDTO(any(User.class))).thenReturn(
                new EmployeeResponseDTO(
                        employeeId,
                        organizationId,
                        organization.getName(),
                        departmentId,
                        department.getName(),
                        department.getCode(),
                        "updated@example.com",
                        "Updated",
                        "Name",
                        true,
                        List.of(),
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now(),
                        null,
                        null
                )
        );

        // When
        EmployeeResponseDTO result = employeeService.updateEmployee(organizationId, employeeId, updateDTO, "admin@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("updated@example.com");
        verify(userRepository).save(employee);
        verify(auditLogService).logEmployeeUpdated(
            eq(organizationId),
            eq(employeeId),
            eq("admin@example.com"),
            anyString(), anyString(),
            any(), any(),
            anyBoolean(), anyBoolean()
        );
    }

    @Test
    @DisplayName("Should list employees with pagination")
    void listEmployees_shouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(employee), pageable, 1);
        
        when(permissionChecker.hasRole(organizationId, "admin@example.com", "DEPARTMENT_MANAGER")).thenReturn(false);
        when(userRepository.findByOrganizationIdAndFilters(organizationId, null, null, pageable)).thenReturn(userPage);
        EmployeeResponseDTO employeeDto = new EmployeeResponseDTO(
                employee.getId(),
                organizationId,
                organization.getName(),
                departmentId,
                department.getName(),
                department.getCode(),
                "employee@example.com",
                "Test",
                "User",
                true,
                List.of(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null,
                null
        );
        when(employeeMapper.toResponsePage(userPage, organizationId)).thenReturn(
                new PageImpl<>(List.of(employeeDto), pageable, 1)
        );

        // When
        Page<EmployeeResponseDTO> result = employeeService.listEmployees(organizationId, null, null, pageable, "admin@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByOrganizationIdAndFilters(organizationId, null, null, pageable);
        verify(employeeMapper).toResponsePage(userPage, organizationId);
    }

    @Test
    @DisplayName("Should deactivate employee when user has permission")
    void deactivateEmployee_shouldSucceed_whenUserHasPermission() {
        // Given
        UUID employeeId = employee.getId();
        doNothing().when(permissionChecker).requireRole(organizationId, "admin@example.com", "HR_ADMIN", "ORG_ADMIN");
        when(userRepository.findByIdAndOrganizationId(employeeId, organizationId)).thenReturn(Optional.of(employee));
        doNothing().when(employeeDomainService).validateDeactivation(employeeId);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        employeeService.deactivateEmployee(organizationId, employeeId, "admin@example.com");

        // Then
        assertThat(employee.isActive()).isFalse();
        verify(userRepository).save(employee);
        verify(auditLogService).logEmployeeDeactivated(organizationId, employeeId, "admin@example.com");
    }
}
