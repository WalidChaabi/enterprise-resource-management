package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete resource allocation view for managers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAllocationDTO {
    private List<EmployeeAllocationDTO> employees;
    private List<ProjectAllocationDTO> projects;
    
    // Summary metrics
    private int totalEmployees;
    private int availableEmployees;
    private int overallocatedEmployees;
    private int underutilizedEmployees;
    
    private int totalProjects;
    private int wellStaffedProjects;
    private int understaffedProjects;
    private int projectsWithoutResources;
}
