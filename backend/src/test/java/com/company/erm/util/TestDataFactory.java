package com.company.erm.util;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.entity.UserRoleId;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory class for creating test data entities.
 * Simplifies test setup by providing pre-configured test objects.
 */
public class TestDataFactory {

    public static Organization createOrganization() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName("Test Organization");
        org.setCode("TEST");
        org.setActive(true);
        return org;
    }

    public static Organization createOrganization(UUID id, String name, String code) {
        Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setCode(code);
        org.setActive(true);
        return org;
    }

    public static Department createDepartment(Organization organization) {
        Department dept = new Department();
        dept.setId(UUID.randomUUID());
        dept.setOrganization(organization);
        dept.setName("Test Department");
        dept.setCode("TEST_DEPT");
        dept.setActive(true);
        return dept;
    }

    public static Department createDepartment(Organization organization, String name, String code) {
        Department dept = new Department();
        dept.setId(UUID.randomUUID());
        dept.setOrganization(organization);
        dept.setName(name);
        dept.setCode(code);
        dept.setActive(true);
        return dept;
    }

    public static Role createRole(Organization organization, String code, String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setOrganization(organization);
        role.setCode(code);
        role.setName(name);
        role.setDescription("Test role");
        role.setScope("ORG");
        return role;
    }

    public static User createUser(Organization organization, Department department, PasswordEncoder passwordEncoder) {
        return createUser(organization, department, "test@example.com", "password123", passwordEncoder);
    }

    public static User createUser(
            Organization organization,
            Department department,
            String email,
            String password,
            PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(organization);
        user.setDepartment(department);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        return user;
    }

    public static User createInactiveUser(Organization organization, Department department, PasswordEncoder passwordEncoder) {
        User user = createUser(organization, department, passwordEncoder);
        user.setActive(false);
        return user;
    }

    public static UserRole createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId(
            user.getId(),
            role.getId(),
            user.getOrganization().getId()
        );
        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setOrganization(user.getOrganization());
        userRole.setAssignedAt(LocalDateTime.now());
        return userRole;
    }
}
