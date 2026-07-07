package com.company.erm.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.AuthCookieService;
import com.company.erm.security.JwtTokenProvider;
import com.company.erm.util.TestDataFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationSecurityService Tests")
class OrganizationSecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthCookieService authCookieService;

    @InjectMocks
    private OrganizationSecurityService organizationSecurityService;

    private Organization primaryOrganization;
    private Organization secondaryOrganization;
    private User user;
    private Role hrAdminRole;
    private UserRole hrAdminInPrimaryOrg;
    private UserRole hrAdminInSecondaryOrg;

    @BeforeEach
    void setUp() {
        var passwordEncoder = new BCryptPasswordEncoder();
        primaryOrganization = TestDataFactory.createOrganization();
        secondaryOrganization = TestDataFactory.createOrganization();
        Department department = TestDataFactory.createDepartment(primaryOrganization);
        user = TestDataFactory.createUser(primaryOrganization, department, "user@example.com", "password", passwordEncoder);
        hrAdminRole = TestDataFactory.createRole(primaryOrganization, "HR_ADMIN", "HR Administrator");
        hrAdminInPrimaryOrg = TestDataFactory.createUserRole(user, hrAdminRole);

        Role secondaryHrRole = TestDataFactory.createRole(secondaryOrganization, "HR_ADMIN", "HR Administrator");
        hrAdminInSecondaryOrg = new UserRole();
        hrAdminInSecondaryOrg.setUser(user);
        hrAdminInSecondaryOrg.setRole(secondaryHrRole);
        hrAdminInSecondaryOrg.setOrganization(secondaryOrganization);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("hasOrgMembership uses user_roles, not users.organization_id")
    void hasOrgMembership_shouldAllowWhenUserRoleExistsInTargetOrg() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), secondaryOrganization.getId()))
                .thenReturn(List.of(hrAdminInSecondaryOrg));

        assertThat(organizationSecurityService.hasOrgMembership(user.getEmail(), secondaryOrganization.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("hasOrgMembership returns false when user has no roles in organization")
    void hasOrgMembership_shouldDenyWhenNoRolesInOrg() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), secondaryOrganization.getId()))
                .thenReturn(List.of());

        assertThat(organizationSecurityService.hasOrgMembership(user.getEmail(), secondaryOrganization.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("hasAnyOrgRole returns true when user has required role in organization")
    void hasAnyOrgRole_shouldAllowWhenRolePresentInOrg() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), primaryOrganization.getId()))
                .thenReturn(List.of(hrAdminInPrimaryOrg));

        assertThat(organizationSecurityService.hasAnyOrgRole(
                user.getEmail(), primaryOrganization.getId(), "HR_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("hasAnyOrgRole returns false when role exists only in another organization")
    void hasAnyOrgRole_shouldDenyWhenRoleNotInTargetOrg() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), secondaryOrganization.getId()))
                .thenReturn(List.of());

        assertThat(organizationSecurityService.hasAnyOrgRole(
                user.getEmail(), secondaryOrganization.getId(), "HR_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("requireAnyOrgRole throws when user lacks membership")
    void requireAnyOrgRole_shouldThrowWhenNotMember() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), secondaryOrganization.getId()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> organizationSecurityService.requireAnyOrgRole(
                user.getEmail(), secondaryOrganization.getId(), "HR_ADMIN"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("does not have access to organization");
    }

    @Test
    @DisplayName("SpEL entry point resolves current user from SecurityContext")
    void hasOrgAccess_shouldUseAuthenticatedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), primaryOrganization.getId()))
                .thenReturn(List.of(hrAdminInPrimaryOrg));

        assertThat(organizationSecurityService.hasOrgAccess(primaryOrganization.getId())).isTrue();
    }

    @Test
    @DisplayName("getOrgRoleCodes returns role codes for organization")
    void getOrgRoleCodes_shouldReturnAssignedRoles() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), primaryOrganization.getId()))
                .thenReturn(List.of(hrAdminInPrimaryOrg));

        assertThat(organizationSecurityService.getOrgRoleCodes(user.getEmail(), primaryOrganization.getId()))
                .isEqualTo(Set.of("HR_ADMIN"));
    }
}
