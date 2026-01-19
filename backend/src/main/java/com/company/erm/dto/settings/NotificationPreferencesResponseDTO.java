package com.company.erm.dto.settings;

import lombok.Data;

@Data
public class NotificationPreferencesResponseDTO {
    private boolean emailEnabled;
    private boolean inAppEnabled;
}
