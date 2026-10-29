package com.bankingsystem.notificationservice.dto;

public record UnreadNotificationCountResponse(
        Long userId,
        long unreadCount
) {
}