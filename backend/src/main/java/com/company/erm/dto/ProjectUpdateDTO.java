package com.company.erm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.company.erm.entity.ProjectStatus;

@Getter
@Setter
public class ProjectUpdateDTO {

    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate endDate;
    private BigDecimal cost;
}
