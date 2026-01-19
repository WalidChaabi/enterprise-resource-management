package com.company.erm.mapper;

import com.company.erm.dto.RoleResponseDTO;
import com.company.erm.entity.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Role entities and DTOs.
 */
@Component
public class RoleMapper {
    
    /**
     * Convert Role entity to RoleResponseDTO.
     */
    public RoleResponseDTO toResponseDTO(Role role) {
        if (role == null) {
            return null;
        }
        
        return new RoleResponseDTO(
            role.getId(),
            role.getOrganization() != null ? role.getOrganization().getId() : null,
            role.getOrganization() != null ? role.getOrganization().getName() : null,
            role.getCode(),
            role.getName(),
            role.getDescription(),
            role.getScope(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }
}

