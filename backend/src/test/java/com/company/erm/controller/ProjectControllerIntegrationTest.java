package com.company.erm.controller;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Project;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.ProjectRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProjectController.
 * 
 * Tests the full project management flow including:
 * - Project creation with authentication
 * - Project listing
 * - Project archiving
 * - Security enforcement
 * - Multi-tenant isolation
 * - Date validation
 */
@SpringBootTest(classes = com.example.erm_backend.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@DisplayName("ProjectController Integration Tests")
class ProjectControllerIntegrationTest {

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
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Organization organization;
    private Department department;
    private Role orgAdminRole;
    private Role projectManagerRole;
    private Role employeeRole;
    private User adminUser;
    private User projectManagerUser;
    private User regularUser;
    private String adminToken;
    private String projectManagerToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        // Clean up
        projectRepository.deleteAll();
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

        orgAdminRole = TestDataFactory.createRole(organization, "ORG_ADMIN", "Organization Administrator");
        roleRepository.save(orgAdminRole);

        projectManagerRole = TestDataFactory.createRole(organization, "PROJECT_MANAGER", "Project Manager");
        roleRepository.save(projectManagerRole);

        employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleRepository.save(employeeRole);

        // Create admin user
        adminUser = TestDataFactory.createUser(organization, department, "admin@example.com", "password123", passwordEncoder);
        userRepository.save(adminUser);
        UserRole adminUserRole = TestDataFactory.createUserRole(adminUser, orgAdminRole);
        userRoleRepository.save(adminUserRole);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), organization.getId());

        // Create project manager user
        projectManagerUser = TestDataFactory.createUser(organization, department, "pm@example.com", "password123", passwordEncoder);
        userRepository.save(projectManagerUser);
        UserRole pmUserRole = TestDataFactory.createUserRole(projectManagerUser, projectManagerRole);
        userRoleRepository.save(pmUserRole);
        projectManagerToken = jwtTokenProvider.generateToken(projectManagerUser.getId(), projectManagerUser.getEmail(), organization.getId());

        // Create regular user
        regularUser = TestDataFactory.createUser(organization, department, "user@example.com", "password123", passwordEncoder);
        userRepository.save(regularUser);
        UserRole regularUserRole = TestDataFactory.createUserRole(regularUser, employeeRole);
        userRoleRepository.save(regularUserRole);
        regularUserToken = jwtTokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), organization.getId());
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 201 when user has ORG_ADMIN role")
    void createProject_shouldReturn201_whenUserHasOrgAdminRole() throws Exception {
        // Given
        com.company.erm.dto.ProjectCreateDTO createDTO = new com.company.erm.dto.ProjectCreateDTO();
        createDTO.setName("ERP Migration");
        createDTO.setDescription("Migration to ERP v2");
        createDTO.setStartDate(LocalDate.now().plusDays(30));
        createDTO.setDeadline(LocalDate.now().plusDays(180));
        createDTO.setBudget(new BigDecimal("150000.00"));
        createDTO.setManagerId(adminUser.getId());
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("ERP Migration"))
                .andExpect(jsonPath("$.description").value("Migration to ERP v2"))
                .andExpect(jsonPath("$.status").value("PLANNING"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 201 when user has PROJECT_MANAGER role")
    void createProject_shouldReturn201_whenUserHasProjectManagerRole() throws Exception {
        // Given
        com.company.erm.dto.ProjectCreateDTO createDTO = new com.company.erm.dto.ProjectCreateDTO();
        createDTO.setName("Website Redesign");
        createDTO.setDescription("Complete website redesign project");
        createDTO.setStartDate(LocalDate.now().plusDays(15));
        createDTO.setDeadline(LocalDate.now().plusDays(90));
        createDTO.setBudget(new BigDecimal("50000.00"));
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + projectManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Website Redesign"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 403 when user lacks permission")
    void createProject_shouldReturn403_whenUserLacksPermission() throws Exception {
        // Given
        com.company.erm.dto.ProjectCreateDTO createDTO = new com.company.erm.dto.ProjectCreateDTO();
        createDTO.setName("New Project");
        createDTO.setDescription("Test project");
        createDTO.setStartDate(LocalDate.now().plusDays(30));
        createDTO.setDeadline(LocalDate.now().plusDays(180));
        createDTO.setBudget(new BigDecimal("10000.00"));
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 400 when dates are invalid")
    void createProject_shouldReturn400_whenDatesInvalid() throws Exception {
        // Given - deadline before start date
        com.company.erm.dto.ProjectCreateDTO createDTO = new com.company.erm.dto.ProjectCreateDTO();
        createDTO.setName("Invalid Project");
        createDTO.setDescription("Project with invalid dates");
        createDTO.setStartDate(LocalDate.now().plusDays(30));
        createDTO.setDeadline(LocalDate.now().plusDays(10)); // Deadline before start date
        createDTO.setBudget(new BigDecimal("10000.00"));
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/projects should return 200 with list of projects")
    void listProjects_shouldReturn200_withListOfProjects() throws Exception {
        // Given - create a project first
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setStatus(com.company.erm.entity.ProjectStatus.PLANNING);
        project.setStartDate(LocalDate.now().plusDays(30));
        project.setDeadline(LocalDate.now().plusDays(180));
        project.setBudget(new BigDecimal("100000.00"));
        project.setOrganization(organization);
        project.setArchived(false);
        projectRepository.save(project);

        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/projects should return empty list when no projects exist")
    void listProjects_shouldReturnEmptyList_whenNoProjectsExist() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/projects should not return archived projects")
    void listProjects_shouldNotReturnArchivedProjects() throws Exception {
        // Given - create an archived project
        Project archivedProject = new Project();
        archivedProject.setName("Archived Project");
        archivedProject.setDescription("This project is archived");
        archivedProject.setStatus(com.company.erm.entity.ProjectStatus.COMPLETED);
        archivedProject.setStartDate(LocalDate.now().minusDays(100));
        archivedProject.setDeadline(LocalDate.now().minusDays(10));
        archivedProject.setBudget(new BigDecimal("50000.00"));
        archivedProject.setOrganization(organization);
        archivedProject.setArchived(true);
        projectRepository.save(archivedProject);

        // When/Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{orgId}/projects/{id} should return 200 when archive successful")
    void archiveProject_shouldReturn200_whenArchiveSuccessful() throws Exception {
        // Given
        Project project = new Project();
        project.setName("Project to Archive");
        project.setDescription("This project will be archived");
        project.setStatus(com.company.erm.entity.ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setDeadline(LocalDate.now().plusDays(60));
        project.setBudget(new BigDecimal("75000.00"));
        project.setOrganization(organization);
        project.setArchived(false);
        projectRepository.save(project);

        // When/Then
        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/projects/{projectId}", 
                        organization.getId(), project.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify project is archived
        Project archived = projectRepository.findById(project.getId()).orElseThrow();
        assert archived.isArchived();
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{orgId}/projects/{id} should return 404 when project not found")
    void archiveProject_shouldReturn404_whenProjectNotFound() throws Exception {
        // Given - use a non-existent UUID (controller expects UUID)
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/projects/{projectId}", 
                        organization.getId(), nonExistentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{orgId}/projects/{id} should return 403 when user lacks permission")
    void archiveProject_shouldReturn403_whenUserLacksPermission() throws Exception {
        // Given
        Project project = new Project();
        project.setName("Project");
        project.setDescription("Test");
        project.setStatus(com.company.erm.entity.ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.now());
        project.setDeadline(LocalDate.now().plusDays(60));
        project.setBudget(new BigDecimal("50000.00"));
        project.setOrganization(organization);
        project.setArchived(false);
        projectRepository.save(project);

        // When/Then - PROJECT_MANAGER cannot archive (only ORG_ADMIN can)
        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/projects/{projectId}", 
                        organization.getId(), project.getId())
                        .header("Authorization", "Bearer " + projectManagerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 401 when not authenticated")
    void createProject_shouldReturn401_whenNotAuthenticated() throws Exception {
        // Given
        com.company.erm.dto.ProjectCreateDTO createDTO = new com.company.erm.dto.ProjectCreateDTO();
        createDTO.setName("Unauthorized Project");
        createDTO.setDescription("This should fail");
        createDTO.setStartDate(LocalDate.now().plusDays(30));
        createDTO.setDeadline(LocalDate.now().plusDays(180));
        createDTO.setBudget(new BigDecimal("10000.00"));
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/projects should return 400 when request body is invalid")
    void createProject_shouldReturn400_whenRequestBodyInvalid() throws Exception {
        // Given - missing required fields
        String invalidRequestBody = "{\"name\":\"\"}";

        // When/Then
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/projects", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }
}
