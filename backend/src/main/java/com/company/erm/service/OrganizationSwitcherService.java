package com.company.erm.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.OrganizationSummaryDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.reporting.DashboardService;

@Service
@Transactional
public class OrganizationSwitcherService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthService authService;
    private final DashboardService dashboardService;

    public OrganizationSwitcherService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            AuthService authService,
            DashboardService dashboardService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.authService = authService;
        this.dashboardService = dashboardService;
    }

    @Transactional(readOnly = true)
    public List<OrganizationSummaryDTO> listOrganizationsForUser(String currentUserEmail) {
        User user = loadUser(currentUserEmail);

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        if (userRoles.isEmpty()) {
            Organization org = user.getOrganization();
            return List.of(new OrganizationSummaryDTO(org.getId(), org.getName()));
        }

        return userRoles.stream()
            .map(UserRole::getOrganization)
            .distinct()
            .map(org -> new OrganizationSummaryDTO(org.getId(), org.getName()))
            .toList();
    }

    public AuthSession switchOrganization(String currentUserEmail, UUID orgId) {
        User user = loadUser(currentUserEmail);
        validateOrganizationAccess(user, orgId);
        dashboardService.evictDashboard(orgId);
        return authService.buildSessionForOrganization(user, orgId);
    }

    @Transactional(readOnly = true)
    public User loadUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found"));
    }

    private void validateOrganizationAccess(User user, UUID orgId) {
        boolean hasAccess = !userRoleRepository
            .findByUserIdAndOrganizationId(user.getId(), orgId)
            .isEmpty();

        if (!hasAccess) {
            throw new BusinessException(
                "ORG_ACCESS_DENIED",
                "User does not have access to the requested organization",
                HttpStatus.FORBIDDEN
            );
        }
    }
}
