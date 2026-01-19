package com.company.erm.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSwitchResponseDTO {
    private UUID activeOrgId;
    private String accessToken;
    private String tokenType;
}
