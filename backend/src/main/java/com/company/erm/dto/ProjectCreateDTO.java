package com.company.erm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ProjectCreateDTO {

    @NotBlank
    private String name;
    private String description;

    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate deadline;

    private BigDecimal budget;

    private UUID managerId;
    private Set<UUID> memberIds;
}
