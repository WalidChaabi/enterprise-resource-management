package com.company.erm.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.MockMvcTestConfiguration;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.company.erm.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(MockMvcTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("SearchController Integration Tests")
class SearchControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Organization organization;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();

        organization = TestDataFactory.createOrganization();
        organizationRepository.save(organization);

        Department department = TestDataFactory.createDepartment(organization);
        departmentRepository.save(department);

        Role hrAdminRole = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        roleRepository.save(hrAdminRole);

        adminUser = TestDataFactory.createUser(organization, department, "admin@example.com", "password", passwordEncoder);
        userRepository.save(adminUser);

        UserRole userRole = TestDataFactory.createUserRole(adminUser, hrAdminRole);
        userRoleRepository.save(userRole);

        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), organization.getId());
    }

    @Test
    @DisplayName("GET search should return 403 for another organization")
    void search_shouldReturn403_whenUserHasNoMembershipInTargetOrg() throws Exception {
        Organization otherOrganization = TestDataFactory.createOrganization(
                UUID.randomUUID(), "Other Organization", "SEARCH-OTHER");
        organizationRepository.save(otherOrganization);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/search", otherOrganization.getId())
                        .param("query", "test")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
