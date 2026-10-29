package com.bankingsystem.notificationservice.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String eventId,
        String notificationType,
        String title,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {
}