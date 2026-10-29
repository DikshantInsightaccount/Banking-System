package com.bankingsystem.notificationservice.repository;

import com.bankingsystem.notificationservice.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Notification>
    findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(
            Long userId
    );

    long countByUserIdAndReadFlagFalse(
            Long userId
    );

    boolean existsByEventIdAndUserIdAndNotificationType(
            String eventId,
            Long userId,
            String notificationType
    );
}