package com.company.erm.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrganizationSwitchRequestDTO {
    @NotNull(message = "orgId is required")
    private UUID orgId;
}
