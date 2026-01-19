package com.company.erm.dto.settings;

import java.util.UUID;

import lombok.Data;

@Data
public class OrganizationSettingsResponseDTO {
    private UUID id;
    private String name;
    private String code;
    private boolean active;
}
