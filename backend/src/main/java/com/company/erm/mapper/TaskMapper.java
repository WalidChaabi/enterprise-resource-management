package com.company.erm.mapper;

import com.company.erm.dto.TaskResponseDTO;
import com.company.erm.entity.Task;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Task entities and Task DTOs.
 */
@Component
public class TaskMapper {

    /**
     * Convert Task entity to TaskResponseDTO.
     * 
     * Note: There's a known type mismatch issue - Project entity uses Long ID
     * but database uses UUID. The projectId in the DTO will be set to null until
     * the Project entity is fixed to use UUID consistently.
     */
    public TaskResponseDTO toResponseDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        
        // Project information
        if (task.getProject() != null) {
            // TODO: Fix when Project entity is updated to use UUID instead of Long
            // For now, projectId is set to null due to type mismatch
            // The project name is still included for reference
            dto.setProjectId(null);
            dto.setProjectName(task.getProject().getName());
        }
        
        // Assigned user information
        if (task.getAssignedTo() != null) {
            dto.setAssignedToId(task.getAssignedTo().getId());
            dto.setAssignedToName(task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName());
        }
        
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        
        return dto;
    }
}

