package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.dto.NotificationResponse;
import com.bankingsystem.notificationservice.dto.UnreadNotificationCountResponse;
import com.bankingsystem.notificationservice.entity.Notification;
import com.bankingsystem.notificationservice.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository
            notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository =
                notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getNotifications(
            Long userId
    ) {
        validateUserId(userId);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getUnreadNotifications(
            Long userId
    ) {
        validateUserId(userId);

        return notificationRepository
                .findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse
    getUnreadCount(
            Long userId
    ) {
        validateUserId(userId);

        long count =
                notificationRepository
                        .countByUserIdAndReadFlagFalse(
                                userId
                        );

        return new UnreadNotificationCountResponse(
                userId,
                count
        );
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long userId,
            Long notificationId
    ) {
        validateUserId(userId);

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID is required"
            );
        }

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification was not found"
                                )
                        );

        if (!userId.equals(
                notification.getUserId()
        )) {
            throw new IllegalArgumentException(
                    "Notification does not belong to this user"
            );
        }

        notification.setReadFlag(true);

        Notification updated =
                notificationRepository.save(
                        notification
                );

        return toResponse(updated);
    }

    @Transactional
    public int markAllAsRead(
            Long userId
    ) {
        validateUserId(userId);

        List<Notification> unreadNotifications =
                notificationRepository
                        .findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(
                                userId
                        );

        unreadNotifications.forEach(
                notification ->
                        notification.setReadFlag(
                                true
                        )
        );

        notificationRepository.saveAll(
                unreadNotifications
        );

        return unreadNotifications.size();
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getEventId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                Boolean.TRUE.equals(
                        notification.getReadFlag()
                ),
                notification.getCreatedAt()
        );
    }

    private void validateUserId(
            Long userId
    ) {
        if (userId == null ||
                userId <= 0) {

            throw new IllegalArgumentException(
                    "Valid user ID is required"
            );
        }
    }
}