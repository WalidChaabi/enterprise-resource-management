package com.company.erm.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.OrganizationSummaryDTO;
import com.company.erm.dto.OrganizationSwitchResponseDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.JwtTokenProvider;

/**
 * Service for listing organizations and switching active organization.
 */
@Service
@Transactional
public class OrganizationSwitcherService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider tokenProvider;

    public OrganizationSwitcherService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public List<OrganizationSummaryDTO> listOrganizationsForUser(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

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

    public OrganizationSwitchResponseDTO switchOrganization(String currentUserEmail, UUID orgId) {
        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

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

        String token = tokenProvider.generateToken(
            user.getId(),
            user.getEmail(),
            orgId
        );

        return new OrganizationSwitchResponseDTO(orgId, token, "Bearer");
    }
}
