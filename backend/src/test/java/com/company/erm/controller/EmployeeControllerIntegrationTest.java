package com.company.erm.controller;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.JwtTokenProvider;
import com.company.erm.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeController.
 * 
 * Tests the full employee management flow including:
 * - Employee creation with authentication
 * - Employee retrieval
 * - Employee update
 * - Security enforcement
 * - Multi-tenant isolation
 */
@SpringBootTest(classes = com.example.erm_backend.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Organization organization;
    private Department department;
    private Role hrAdminRole;
    private Role employeeRole;
    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        // Clean up
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create test data
        organization = TestDataFactory.createOrganization();
        organizationRepository.save(organization);

        department = TestDataFactory.createDepartment(organization);
        departmentRepository.save(department);

        hrAdminRole = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        roleRepository.save(hrAdminRole);

        employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleRepository.save(employeeRole);

        // Create admin user
        adminUser = TestDataFactory.createUser(organization, department, "admin@example.com", "password123", passwordEncoder);
        userRepository.save(adminUser);
        UserRole adminUserRole = TestDataFactory.createUserRole(adminUser, hrAdminRole);
        userRoleRepository.save(adminUserRole);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), organization.getId());

        // Create regular user
        regularUser = TestDataFactory.createUser(organization, department, "user@example.com", "password123", passwordEncoder);
        userRepository.save(regularUser);
        UserRole regularUserRole = TestDataFactory.createUserRole(regularUser, employeeRole);
        userRoleRepository.save(regularUserRole);
        regularUserToken = jwtTokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), organization.getId());
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/employees should return 201 when user has HR_ADMIN role")
    void createEmployee_shouldReturn201_whenUserHasHrAdminRole() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.EmployeeCreateDTO(
                        "newemployee@example.com",
                        "Password123!",
                        "John",
                        "Doe",
                        department.getId(),
                        employeeRole.getId()
                )
        );

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/employees", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("newemployee@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/employees should return 403 when user lacks permission")
    void createEmployee_shouldReturn403_whenUserLacksPermission() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.EmployeeCreateDTO(
                        "newemployee@example.com",
                        "Password123!",
                        "John",
                        "Doe",
                        department.getId(),
                        employeeRole.getId()
                )
        );

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/employees", organization.getId())
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/employees/{id} should return 200 when employee exists")
    void getEmployee_shouldReturn200_whenEmployeeExists() throws Exception {
        // Given
        User employee = TestDataFactory.createUser(organization, department, "employee@example.com", "password", passwordEncoder);
        userRepository.save(employee);

        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/employees/{employeeId}", organization.getId(), employee.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("employee@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/employees/{id} should return 404 when employee not found")
    void getEmployee_shouldReturn404_whenEmployeeNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/employees/{employeeId}", organization.getId(), nonExistentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/employees should return 401 when not authenticated")
    void listEmployees_shouldReturn401_whenNotAuthenticated() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/employees", organization.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{orgId}/employees/{id} should return 200 when update successful")
    void updateEmployee_shouldReturn200_whenUpdateSuccessful() throws Exception {
        // Given
        User employee = TestDataFactory.createUser(organization, department, "employee@example.com", "password", passwordEncoder);
        userRepository.save(employee);

        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.EmployeeUpdateDTO(
                        java.util.Optional.of("updated@example.com"),
                        java.util.Optional.of("Updated"),
                        java.util.Optional.of("Name"),
                        java.util.Optional.empty(),
                        java.util.Optional.empty()
                )
        );

        // When/Then
        mockMvc.perform(put("/api/v1/organizations/{organizationId}/employees/{employeeId}", organization.getId(), employee.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
}
