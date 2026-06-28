package com.company.erm.mapper;

import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User entities and Employee DTOs.
 */
@Component
public class EmployeeMapper {
    
    private final UserRoleRepository userRoleRepository;
    
    public EmployeeMapper(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }
    
    /**
     * Convert User entity to EmployeeResponseDTO.
     */
    public EmployeeResponseDTO toResponseDTO(User user) {
        return toResponseDTO(user, userRoleRepository.findByUser(user));
    }

    /**
     * Convert a page of users using a single batch query for roles.
     */
    public Page<EmployeeResponseDTO> toResponsePage(Page<User> users, UUID organizationId) {
        List<User> content = users.getContent();
        if (content.isEmpty()) {
            return users.map(user -> toResponseDTO(user, List.of()));
        }

        List<UUID> userIds = content.stream().map(User::getId).toList();
        Map<UUID, List<UserRole>> rolesByUserId = userRoleRepository
            .findByUserIdInAndOrganizationId(userIds, organizationId)
            .stream()
            .collect(Collectors.groupingBy(ur -> ur.getId().getUserId()));

        return users.map(user -> toResponseDTO(
            user,
            rolesByUserId.getOrDefault(user.getId(), List.of())
        ));
    }

    private EmployeeResponseDTO toResponseDTO(User user, List<UserRole> userRoles) {
        List<EmployeeResponseDTO.RoleDTO> roles = userRoles.stream()
            .map(this::toRoleDTO)
            .collect(Collectors.toList());
        
        return new EmployeeResponseDTO(
            user.getId(),
            user.getOrganization().getId(),
            user.getOrganization().getName(),
            user.getDepartment() != null ? user.getDepartment().getId() : null,
            user.getDepartment() != null ? user.getDepartment().getName() : null,
            user.getDepartment() != null ? user.getDepartment().getCode() : null,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isActive(),
            roles,
            user.getCreatedAt(),
            user.getUpdatedAt(),
            null,
            null
        );
    }
    
    private EmployeeResponseDTO.RoleDTO toRoleDTO(UserRole userRole) {
        return new EmployeeResponseDTO.RoleDTO(
            userRole.getRole().getId(),
            userRole.getRole().getCode(),
            userRole.getRole().getName(),
            userRole.getRole().getScope()
        );
    }
}
