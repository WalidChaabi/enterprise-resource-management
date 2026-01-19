package com.company.erm.mapper;

import com.company.erm.dto.ProjectResponseDTO;
import com.company.erm.entity.Project;

import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponseDTO toDto(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setStatus(project.getStatus());
        dto.setStartDate(project.getStartDate());
        dto.setDeadline(project.getDeadline());
        dto.setManagerName(
            project.getManager() != null ? project.getManager().getFullName() : null
        );
        dto.setMembersCount(project.getMembers().size());
        return dto;
    }
}
