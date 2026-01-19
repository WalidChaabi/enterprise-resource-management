package com.company.erm.dto.settings;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationPreferencesUpdateDTO {
    @NotNull(message = "emailEnabled is required")
    private Boolean emailEnabled;

    @NotNull(message = "inAppEnabled is required")
    private Boolean inAppEnabled;
}
