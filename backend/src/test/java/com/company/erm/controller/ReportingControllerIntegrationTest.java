package com.company.erm.controller;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.company.erm.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(MockMvcTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportingController Integration Tests")
class ReportingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private String adminToken;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();

        organization = TestDataFactory.createOrganization();
        organizationRepository.save(organization);

        Department department = TestDataFactory.createDepartment(organization);
        departmentRepository.save(department);

        Role orgAdminRole = TestDataFactory.createRole(organization, "ORG_ADMIN", "Organization Administrator");
        roleRepository.save(orgAdminRole);

        User adminUser = TestDataFactory.createUser(organization, department, "admin@example.com", "password123", passwordEncoder);
        userRepository.save(adminUser);
        userRoleRepository.save(TestDataFactory.createUserRole(adminUser, orgAdminRole));
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), organization.getId());

        Project project = new Project();
        project.setOrganization(organizationRepository.getReferenceById(organization.getId()));
        project.setName("Dashboard Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setArchived(false);
        project.setBudget(new BigDecimal("10000"));
        project.setCost(new BigDecimal("2500"));
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setDeadline(LocalDate.now().plusDays(60));
        projectRepository.save(project);
    }

    @Test
    @DisplayName("GET /reports/dashboard should return aggregated stats for authorized user")
    void getDashboard_shouldReturn200_withProjectCounts() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/dashboard", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(1))
                .andExpect(jsonPath("$.activeProjects").value(1))
                .andExpect(jsonPath("$.totalEmployees").value(1))
                .andExpect(jsonPath("$.activeEmployees").value(1));
    }

    @Test
    @DisplayName("GET /reports/dashboard should return 403 for user without reporting role")
    void getDashboard_shouldReturn403_forUnauthorizedUser() throws Exception {
        Department department = departmentRepository.findAll().get(0);
        Role employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleRepository.save(employeeRole);

        User employee = TestDataFactory.createUser(organization, department, "emp@example.com", "password123", passwordEncoder);
        userRepository.save(employee);
        userRoleRepository.save(TestDataFactory.createUserRole(employee, employeeRole));
        String employeeToken = jwtTokenProvider.generateToken(employee.getId(), employee.getEmail(), organization.getId());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/dashboard", organization.getId())
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
}
