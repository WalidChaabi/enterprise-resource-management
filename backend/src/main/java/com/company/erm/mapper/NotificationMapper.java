package com.company.erm.mapper;

import org.springframework.stereotype.Component;

import com.company.erm.dto.NotificationResponseDTO;
import com.company.erm.entity.Notification;

/**
 * Mapper for converting between Notification entities and Notification DTOs.
 */
@Component
public class NotificationMapper {

    public NotificationResponseDTO toResponseDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
        }
        dto.setTitle(notification.getTitle());
        dto.setDescription(notification.getDescription());
        dto.setTime(notification.getTime());
        dto.setUnread(notification.isUnread());
        return dto;
    }
}
