package com.company.erm.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.company.erm.entity.ProjectStatus;

@Getter
@Setter
public class ProjectUpdateDTO {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private ProjectStatus status;

    private LocalDate endDate;

    private BigDecimal cost;

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isHasUpdates() {
        return name != null || description != null || status != null || endDate != null || cost != null;
    }
}
