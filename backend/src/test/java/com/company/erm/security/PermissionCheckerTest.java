package com.company.erm.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import com.company.erm.util.TestDataFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionChecker Tests")
class PermissionCheckerTest {

    @Mock
    private OrganizationSecurityService organizationSecurity;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PermissionChecker permissionChecker;

    private Organization organization;
    private Organization otherOrganization;
    private Department department;
    private User user;
    private Role hrAdminRole;
    private UserRole hrAdminUserRole;

    @BeforeEach
    void setUp() {
        var passwordEncoder = new BCryptPasswordEncoder();
        organization = TestDataFactory.createOrganization();
        otherOrganization = TestDataFactory.createOrganization();
        department = TestDataFactory.createDepartment(organization);
        user = TestDataFactory.createUser(organization, department, "manager@example.com", "password", passwordEncoder);
        hrAdminRole = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        hrAdminUserRole = TestDataFactory.createUserRole(user, hrAdminRole);
    }

    @Test
    @DisplayName("requireRole delegates to OrganizationSecurityService")
    void requireRole_shouldDelegateToOrganizationSecurity() {
        doNothing().when(organizationSecurity)
                .requireAnyOrgRole(user.getEmail(), organization.getId(), "HR_ADMIN");

        permissionChecker.requireRole(organization.getId(), user.getEmail(), "HR_ADMIN");

        verify(organizationSecurity).requireAnyOrgRole(user.getEmail(), organization.getId(), "HR_ADMIN");
    }

    @Test
    @DisplayName("requireRole propagates AccessDeniedException from OrganizationSecurityService")
    void requireRole_shouldThrow_whenOrganizationSecurityDenies() {
        doThrow(new AccessDeniedException("User does not have access to organization: " + otherOrganization.getId()))
                .when(organizationSecurity)
                .requireAnyOrgRole(user.getEmail(), otherOrganization.getId(), "HR_ADMIN");

        assertThatThrownBy(() ->
                permissionChecker.requireRole(otherOrganization.getId(), user.getEmail(), "HR_ADMIN"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("does not have access to organization");
    }

    @Test
    @DisplayName("hasRole delegates to OrganizationSecurityService")
    void hasRole_shouldDelegateToOrganizationSecurity() {
        when(organizationSecurity.hasAnyOrgRole(user.getEmail(), organization.getId(), "HR_ADMIN"))
                .thenReturn(true);

        assertThat(permissionChecker.hasRole(organization.getId(), user.getEmail(), "HR_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("requireDepartmentAccess should allow HR admins for any department")
    void requireDepartmentAccess_shouldAllowHrAdmin() {
        when(organizationSecurity.hasAnyOrgRole(
                user.getEmail(), organization.getId(), RoleCode.HR_ADMIN, RoleCode.ORG_ADMIN))
                .thenReturn(true);

        permissionChecker.requireDepartmentAccess(
                organization.getId(),
                user.getEmail(),
                UUID.randomUUID());
    }

    @Test
    @DisplayName("requireDepartmentAccess should allow department managers for their own department")
    void requireDepartmentAccess_shouldAllowDepartmentManagerForOwnDepartment() {
        when(organizationSecurity.hasAnyOrgRole(
                user.getEmail(), organization.getId(), RoleCode.HR_ADMIN, RoleCode.ORG_ADMIN))
                .thenReturn(false);
        when(organizationSecurity.hasAnyOrgRole(
                user.getEmail(), organization.getId(), RoleCode.DEPARTMENT_MANAGER))
                .thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        permissionChecker.requireDepartmentAccess(
                organization.getId(),
                user.getEmail(),
                department.getId());
    }

    @Test
    @DisplayName("requireDepartmentAccess should deny managers for other departments")
    void requireDepartmentAccess_shouldDenyDepartmentManagerForOtherDepartment() {
        when(organizationSecurity.hasAnyOrgRole(
                user.getEmail(), organization.getId(), RoleCode.HR_ADMIN, RoleCode.ORG_ADMIN))
                .thenReturn(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UUID otherDepartmentId = UUID.randomUUID();

        assertThatThrownBy(() ->
                permissionChecker.requireDepartmentAccess(
                        organization.getId(),
                        user.getEmail(),
                        otherDepartmentId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("User does not have access to department");
    }

    @Test
    @DisplayName("getUserDepartmentId should return the user's department")
    void getUserDepartmentId_shouldReturnDepartmentId() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(permissionChecker.getUserDepartmentId(organization.getId(), user.getEmail()))
                .isEqualTo(department.getId());

        verify(organizationSecurity).requireOrgMembership(user.getEmail(), organization.getId());
    }

    @Test
    @DisplayName("getUserDepartmentId should return null when user has no department")
    void getUserDepartmentId_shouldReturnNull_whenUserHasNoDepartment() {
        user.setDepartment(null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(permissionChecker.getUserDepartmentId(organization.getId(), user.getEmail())).isNull();
    }
}
