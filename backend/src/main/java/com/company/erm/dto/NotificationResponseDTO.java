package com.company.erm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class NotificationResponseDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private LocalDateTime time;
    private boolean unread;
}
