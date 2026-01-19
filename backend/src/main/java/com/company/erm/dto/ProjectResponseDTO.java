package com.company.erm.dto;

import com.company.erm.entity.ProjectStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectResponseDTO {

    private UUID id;
    private String name;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
    private String managerName;
    private int membersCount;
}