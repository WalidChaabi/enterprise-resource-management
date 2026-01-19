package com.company.erm.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.UserNotificationPreferences;

public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences, UUID> {
    Optional<UserNotificationPreferences> findByUserId(UUID userId);
}
