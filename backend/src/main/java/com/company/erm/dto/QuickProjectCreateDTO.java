package com.company.erm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Minimal DTO for quick project creation.
 */
@Getter
@Setter
public class QuickProjectCreateDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private LocalDate startDate;
    private LocalDate deadline;
    private BigDecimal budget;
    private UUID managerId;
    private Set<UUID> memberIds;
}
