package com.company.erm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.SignupRequestDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.entity.UserRoleId;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.RoleRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.RoleCode;
import com.company.erm.service.AuthService.AuthSession;

@Service
public class OrganizationProvisioningService {

    private record RoleTemplate(RoleCode code, String name, String description) {}

    private static final List<RoleTemplate> DEFAULT_ROLES = List.of(
            new RoleTemplate(RoleCode.ORG_ADMIN, "Organization Administrator", "Full organization access"),
            new RoleTemplate(RoleCode.HR_ADMIN, "HR Administrator", "HR management access"),
            new RoleTemplate(RoleCode.PROJECT_MANAGER, "Project Manager", "Project and team management"),
            new RoleTemplate(RoleCode.DEPARTMENT_MANAGER, "Department Manager", "Department management access"),
            new RoleTemplate(RoleCode.EMPLOYEE, "Employee", "Standard employee access")
    );

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final StripeBillingService stripeBillingService;
    private final boolean signupEnabled;

    public OrganizationProvisioningService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            AuthService authService,
            StripeBillingService stripeBillingService,
            @Value("${erm.signup.enabled:true}") boolean signupEnabled) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.stripeBillingService = stripeBillingService;
        this.signupEnabled = signupEnabled;
    }

    @Transactional
    public AuthSession signup(SignupRequestDTO request) {
        if (!signupEnabled) {
            throw new BusinessException("SIGNUP_DISABLED", "Self-serve signup is not available", HttpStatus.FORBIDDEN);
        }
        if (!Boolean.TRUE.equals(request.acceptTerms())) {
            throw new BusinessException("TERMS_REQUIRED", "You must accept the terms of service", HttpStatus.BAD_REQUEST);
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("EMAIL_ALREADY_IN_USE", "An account with this email already exists", HttpStatus.CONFLICT);
        }

        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName(request.organizationName().trim());
        organization.setCode(generateUniqueOrgCode(request.organizationName()));
        organization.setActive(true);
        organization.setPlanCode("TRIAL");
        organization.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        organization.setSeatLimit(5);
        organization = organizationRepository.save(organization);

        List<Role> roles = seedDefaultRoles(organization);
        Role orgAdminRole = roles.stream()
                .filter(r -> RoleCode.ORG_ADMIN.code().equals(r.getCode()))
                .findFirst()
                .orElseThrow();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(organization);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(true);
        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(user.getId(), orgAdminRole.getId(), organization.getId()));
        userRole.setUser(user);
        userRole.setRole(orgAdminRole);
        userRole.setOrganization(organization);
        userRoleRepository.save(userRole);

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setOrganization(organization);
        employee.setUser(user);
        employee.setEmployeeCode("EMP-001");
        employee.setJobTitle("Administrator");
        employee.setActive(true);
        employeeRepository.save(employee);

        stripeBillingService.ensureStripeCustomer(organization, email);

        return authService.buildSessionForOrganization(user, organization.getId());
    }

    private List<Role> seedDefaultRoles(Organization organization) {
        List<Role> saved = new ArrayList<>();
        for (RoleTemplate template : DEFAULT_ROLES) {
            Role role = new Role();
            role.setId(UUID.randomUUID());
            role.setOrganization(organization);
            role.setCode(template.code().code());
            role.setName(template.name());
            role.setDescription(template.description());
            role.setScope("ORG");
            saved.add(roleRepository.save(role));
        }
        return saved;
    }

    private String generateUniqueOrgCode(String organizationName) {
        String base = organizationName.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "ORG";
        }
        if (base.length() > 24) {
            base = base.substring(0, 24);
        }

        String candidate = base;
        int suffix = 0;
        while (organizationRepository.findByCode(candidate).isPresent()) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }
}
