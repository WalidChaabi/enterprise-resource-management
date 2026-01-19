package com.company.erm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ProjectCreateDTO {

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate deadline;

    private BigDecimal budget;

    private UUID managerId;
    private Set<UUID> memberIds;
}
