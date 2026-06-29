package com.company.erm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.NotificationResponseDTO;
import com.company.erm.entity.Notification;
import com.company.erm.entity.User;
import com.company.erm.entity.UserNotificationPreferences;
import com.company.erm.exception.BusinessException;
import com.company.erm.mapper.NotificationMapper;
import com.company.erm.repository.NotificationRepository;
import com.company.erm.repository.UserNotificationPreferencesRepository;
import com.company.erm.repository.UserRepository;

/**
 * Application service for notification use cases.
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository notificationPreferencesRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            UserNotificationPreferencesRepository notificationPreferencesRepository,
            NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Create an in-app notification for a user when in-app notifications are enabled.
     */
    public void createInAppNotification(User user, String title, String description) {
        if (user == null || !isInAppEnabled(user)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setDescription(description);
        notification.setUnread(true);
        notificationRepository.save(notification);
    }

    private boolean isInAppEnabled(User user) {
        return notificationPreferencesRepository.findByUserId(user.getId())
            .map(UserNotificationPreferences::isInAppEnabled)
            .orElse(true);
    }

    /**
     * Get notifications for current user with pagination, sorted by time desc.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getCurrentUserNotifications(
            String currentUserEmail,
            int page,
            int size) {
        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
        return notificationRepository.findByUserIdOrderByTimeDesc(user.getId(), pageable)
            .map(notificationMapper::toResponseDTO);
    }

    /**
     * Mark all notifications as read for current user.
     */
    public void markAllReadForCurrentUser(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        notificationRepository.markAllReadByUserId(user.getId());
    }
}
