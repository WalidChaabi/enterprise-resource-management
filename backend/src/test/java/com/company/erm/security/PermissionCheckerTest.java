package com.company.erm.security;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionChecker Tests")
class PermissionCheckerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

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
    @DisplayName("requireRole should pass when user has an allowed role in the organization")
    void requireRole_shouldPass_whenUserHasRole() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(hrAdminUserRole));

        permissionChecker.requireRole(organization.getId(), user.getEmail(), "HR_ADMIN");
    }

    @Test
    @DisplayName("requireRole should throw when user is not found")
    void requireRole_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionChecker.requireRole(organization.getId(), "missing@example.com", "HR_ADMIN"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("User not found");
    }

    @Test
    @DisplayName("requireRole should throw when user belongs to another organization")
    void requireRole_shouldThrow_whenUserBelongsToDifferentOrganization() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                permissionChecker.requireRole(otherOrganization.getId(), user.getEmail(), "HR_ADMIN"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("User does not belong to organization");
    }

    @Test
    @DisplayName("requireRole should throw when user lacks the required role")
    void requireRole_shouldThrow_whenUserLacksRole() {
        Role employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        UserRole employeeUserRole = TestDataFactory.createUserRole(user, employeeRole);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(employeeUserRole));

        assertThatThrownBy(() ->
                permissionChecker.requireRole(organization.getId(), user.getEmail(), "ORG_ADMIN"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("User does not have required role");
    }

    @Test
    @DisplayName("hasRole should return true when requireRole would pass")
    void hasRole_shouldReturnTrue_whenUserHasRole() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(hrAdminUserRole));

        assertThat(permissionChecker.hasRole(organization.getId(), user.getEmail(), "HR_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("hasRole should return false when requireRole would fail")
    void hasRole_shouldReturnFalse_whenUserLacksRole() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of());

        assertThat(permissionChecker.hasRole(organization.getId(), user.getEmail(), "ORG_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("requireDepartmentAccess should allow HR admins for any department")
    void requireDepartmentAccess_shouldAllowHrAdmin() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(hrAdminUserRole));

        permissionChecker.requireDepartmentAccess(
            organization.getId(),
            user.getEmail(),
            UUID.randomUUID()
        );
    }

    @Test
    @DisplayName("requireDepartmentAccess should allow department managers for their own department")
    void requireDepartmentAccess_shouldAllowDepartmentManagerForOwnDepartment() {
        Role managerRole = TestDataFactory.createRole(organization, "DEPARTMENT_MANAGER", "Department Manager");
        UserRole managerUserRole = TestDataFactory.createUserRole(user, managerRole);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(managerUserRole));

        permissionChecker.requireDepartmentAccess(
            organization.getId(),
            user.getEmail(),
            department.getId()
        );
    }

    @Test
    @DisplayName("requireDepartmentAccess should deny managers for other departments")
    void requireDepartmentAccess_shouldDenyDepartmentManagerForOtherDepartment() {
        Role managerRole = TestDataFactory.createRole(organization, "DEPARTMENT_MANAGER", "Department Manager");
        UserRole managerUserRole = TestDataFactory.createUserRole(user, managerRole);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(managerUserRole));

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
    }

    @Test
    @DisplayName("getUserDepartmentId should return null when user has no department")
    void getUserDepartmentId_shouldReturnNull_whenUserHasNoDepartment() {
        user.setDepartment(null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(permissionChecker.getUserDepartmentId(organization.getId(), user.getEmail())).isNull();
    }
}
