package com.company.erm.dto.settings;

import java.util.List;

import lombok.Data;

@Data
public class EntitlementsResponseDTO {
    private String planCode;
    private String planName;
    private List<String> entitlements;
    private Integer seatLimit;
    private long seatCount;
    private boolean upgradeRequired;
}
