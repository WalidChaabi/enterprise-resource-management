package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeReportDTO {
    private UUID id; // Project ID, Employee ID, or Task ID depending on report type
    private String name; // Project name, Employee name, or Task name
    private BigDecimal totalHours;
    private int entriesCount;
    private BigDecimal averageHoursPerEntry;
}
