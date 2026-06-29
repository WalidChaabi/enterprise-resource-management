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
import com.company.erm.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.company.erm.config.MockMvcTestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for AuthController.
 * 
 * Tests the full authentication flow including:
 * - Successful login
 * - Invalid credentials
 * - Inactive user
 * - Request validation
 */
@SpringBootTest(classes = com.company.erm.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(MockMvcTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

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

    private Organization organization;
    private Department department;
    private Role role;
    private User testUser;

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

        role = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        roleRepository.save(role);

        testUser = TestDataFactory.createUser(organization, department, "test@example.com", "password123", passwordEncoder);
        userRepository.save(testUser);

        UserRole userRole = TestDataFactory.createUserRole(testUser, role);
        userRoleRepository.save(userRole);
    }

    @Test
    @DisplayName("POST /auth/login should return 200 with token for valid credentials")
    void login_shouldReturn200_whenCredentialsValid() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("test@example.com", "password123")
        );

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("HR_ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login should return 401 for invalid email")
    void login_shouldReturn401_whenEmailInvalid() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("nonexistent@example.com", "password123")
        );

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /auth/login should return 401 for invalid password")
    void login_shouldReturn401_whenPasswordInvalid() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("test@example.com", "wrongpassword")
        );

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /auth/login should return 401 for inactive user")
    void login_shouldReturn401_whenUserInactive() throws Exception {
        // Given - create inactive user (avoid updating persisted user without audit timestamps)
        User inactiveUser = TestDataFactory.createInactiveUser(organization, department, passwordEncoder);
        inactiveUser.setEmail("inactive@example.com");
        userRepository.save(inactiveUser);
        UserRole inactiveUserRole = TestDataFactory.createUserRole(inactiveUser, role);
        userRoleRepository.save(inactiveUserRole);

        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("inactive@example.com", "password123")
        );

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("User account is inactive"));
    }

    @Test
    @DisplayName("POST /auth/login should return 400 for invalid request body")
    void login_shouldReturn400_whenRequestBodyInvalid() throws Exception {
        // Given
        String invalidRequestBody = "{\"email\":\"\",\"password\":\"\"}";

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/refresh should return 204 when refresh cookie is valid")
    void refresh_shouldReturn204_whenRefreshCookieValid() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("test@example.com", "password123")
        );

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();

        mockMvc.perform(post("/auth/refresh").cookie(cookies))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /auth/refresh should return 401 when refresh cookie is missing")
    void refresh_shouldReturn401_whenRefreshCookieMissing() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/forgot-password should return 204")
    void forgotPassword_shouldReturn204() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /auth/reset-password should return 400 for invalid token")
    void resetPassword_shouldReturn400_whenTokenInvalid() throws Exception {
        String requestBody = "{\"token\":\"invalid-token\",\"newPassword\":\"newpassword123\"}";

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/logout should return 204 and clear auth cookies")
    void logout_shouldReturn204_andClearCookies() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.company.erm.dto.AuthRequest("test@example.com", "password123")
        );

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();

        mockMvc.perform(post("/auth/logout").cookie(cookies))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }
}
