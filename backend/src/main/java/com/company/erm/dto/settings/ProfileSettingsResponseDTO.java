package com.company.erm.dto.settings;

import java.util.UUID;

import lombok.Data;

@Data
public class ProfileSettingsResponseDTO {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
}
