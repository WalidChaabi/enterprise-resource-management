package com.company.erm.mapper;

import com.company.erm.dto.DepartmentResponseDTO;
import com.company.erm.entity.Department;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Department entities and DTOs.
 */
@Component
public class DepartmentMapper {
    
    /**
     * Convert Department entity to DepartmentResponseDTO.
     */
    public DepartmentResponseDTO toResponseDTO(Department department) {
        return new DepartmentResponseDTO(
            department.getId(),
            department.getOrganization().getId(),
            department.getOrganization().getName(),
            department.getName(),
            department.getCode(),
            department.getParent() != null ? department.getParent().getId() : null,
            department.getParent() != null ? department.getParent().getName() : null,
            department.getParent() != null ? department.getParent().getCode() : null,
            department.isActive(),
            department.getCreatedAt(),
            department.getUpdatedAt(),
            null, // createdBy - would need to be tracked separately
            null  // lastModifiedBy - would need to be tracked separately
        );
    }
}

