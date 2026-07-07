package com.company.erm.support;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

@SpringBootTest(classes = com.company.erm.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(MockMvcTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
public abstract class OrgIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected Organization organization;
    protected Department department;
    protected User adminUser;
    protected String adminToken;

    @BeforeEach
    void seedPrimaryOrganization() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();

        organization = TestDataFactory.createOrganization();
        organizationRepository.save(organization);

        department = TestDataFactory.createDepartment(organization);
        departmentRepository.save(department);

        Role adminRole = TestDataFactory.createRole(organization, "ORG_ADMIN", "Organization Administrator");
        roleRepository.save(adminRole);

        adminUser = TestDataFactory.createUser(
                organization, department, "admin@example.com", "password", passwordEncoder);
        userRepository.save(adminUser);

        UserRole assignment = TestDataFactory.createUserRole(adminUser, adminRole);
        userRoleRepository.save(assignment);

        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), organization.getId());
    }

    protected Organization createOtherOrganization(String code) {
        Organization other = TestDataFactory.createOrganization(UUID.randomUUID(), "Other Org", code);
        return organizationRepository.save(other);
    }

    protected String tokenForOrganization(User user, Organization org) {
        return jwtTokenProvider.generateToken(user.getId(), user.getEmail(), org.getId());
    }
}
