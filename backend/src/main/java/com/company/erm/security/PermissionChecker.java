package com.company.erm.security;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.company.erm.entity.User;
import com.company.erm.repository.UserRepository;

/**
 * Legacy facade delegating org-scoped role checks to {@link OrganizationSecurityService}.
 *
 * @deprecated Inject {@link OrganizationSecurityService} directly in new code (Task 1.4 migration).
 */
@Deprecated(since = "1.0.0", forRemoval = false)
@Service
public class PermissionChecker {

    private final OrganizationSecurityService organizationSecurity;
    private final UserRepository userRepository;

    public PermissionChecker(
            OrganizationSecurityService organizationSecurity,
            UserRepository userRepository) {
        this.organizationSecurity = organizationSecurity;
        this.userRepository = userRepository;
    }

    public void requireRole(UUID organizationId, String userEmail, RoleCode... roles) {
        organizationSecurity.requireAnyOrgRole(
                userEmail,
                organizationId,
                Arrays.stream(roles).map(RoleCode::code).toArray(String[]::new));
    }

    public void requireRole(UUID organizationId, String userEmail, String... roles) {
        organizationSecurity.requireAnyOrgRole(userEmail, organizationId, roles);
    }

    public boolean hasRole(UUID organizationId, String userEmail, String... roles) {
        return organizationSecurity.hasAnyOrgRole(userEmail, organizationId, roles);
    }

    public boolean hasAnyRole(UUID organizationId, String userEmail, RoleCode... roles) {
        return organizationSecurity.hasAnyOrgRole(userEmail, organizationId, roles);
    }

    public void requireDepartmentAccess(UUID organizationId, String userEmail, UUID departmentId) {
        if (organizationSecurity.hasAnyOrgRole(
                userEmail, organizationId, RoleCode.HR_ADMIN, RoleCode.ORG_ADMIN)) {
            return;
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getDepartment() != null && user.getDepartment().getId().equals(departmentId)
                && organizationSecurity.hasAnyOrgRole(
                        userEmail, organizationId, RoleCode.DEPARTMENT_MANAGER)) {
            return;
        }

        throw new AccessDeniedException(
                "User does not have access to department: " + departmentId);
    }

    public UUID getUserDepartmentId(UUID organizationId, String userEmail) {
        organizationSecurity.requireOrgMembership(userEmail, organizationId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getDepartment() != null) {
            return user.getDepartment().getId();
        }

        return null;
    }
}
