package com.company.erm.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByTimeDesc(UUID userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.unread = false WHERE n.user.id = :userId AND n.unread = true")
    int markAllReadByUserId(@Param("userId") UUID userId);
}
