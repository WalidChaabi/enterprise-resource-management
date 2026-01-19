package com.company.erm.mapper;

import com.company.erm.dto.EmployeeResponseDTO;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.repository.UserRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
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
            null, // createdBy - would need to be tracked separately
            null  // lastModifiedBy - would need to be tracked separately
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

