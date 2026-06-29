package com.company.erm.dto.settings;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class BillingSettingsResponseDTO {
    private String planName;
    private String status;
    private LocalDate nextBillingDate;
    private LocalDate trialEndsAt;
    private BigDecimal amount;
    private String currency;
    private long seatCount;
    private boolean portalAvailable;
    private boolean checkoutAvailable;
}
