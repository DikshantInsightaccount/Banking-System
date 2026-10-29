package com.bankingsystem.notificationservice.controller;

import com.bankingsystem.notificationservice.dto.NotificationResponse;
import com.bankingsystem.notificationservice.dto.UnreadNotificationCountResponse;
import com.bankingsystem.notificationservice.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService
            notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>>
    getNotifications(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService
                        .getNotifications(userId)
        );
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>>
    getUnreadNotifications(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService
                        .getUnreadNotifications(
                                userId
                        )
        );
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<UnreadNotificationCountResponse>
    getUnreadCount(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService
                        .getUnreadCount(userId)
        );
    }

    @PatchMapping(
            "/user/{userId}/{notificationId}/read"
    )
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long userId,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(
                        userId,
                        notificationId
                )
        );
    }

    @PatchMapping(
            "/user/{userId}/read-all"
    )
    public ResponseEntity<Map<String, Object>>
    markAllAsRead(
            @PathVariable Long userId
    ) {
        int updatedCount =
                notificationService
                        .markAllAsRead(userId);

        return ResponseEntity.ok(
                Map.of(
                        "userId",
                        userId,

                        "updatedCount",
                        updatedCount,

                        "message",
                        "Notifications marked as read"
                )
        );
    }
}
