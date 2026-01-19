package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Alert DTO for intelligent alerts in the dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private AlertType type;
    private String severity; // HIGH, MEDIUM, LOW
    private String message;
    private UUID projectId;
    private String projectName;
    private UUID employeeId;
    private String employeeName;
    private String details; // Additional context
}
