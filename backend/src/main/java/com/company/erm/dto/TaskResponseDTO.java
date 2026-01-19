package com.company.erm.dto;

import com.company.erm.entity.TaskPriority;
import com.company.erm.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskResponseDTO {

    private UUID id;
    private UUID projectId;
    private String projectName;
    private UUID assignedToId;
    private String assignedToName;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

