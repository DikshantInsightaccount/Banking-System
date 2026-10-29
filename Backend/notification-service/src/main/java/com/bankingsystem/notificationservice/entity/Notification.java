package com.bankingsystem.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event_user_type",
                        columnNames = {
                                "event_id",
                                "user_id",
                                "notification_type"
                        }
                )
        }
)
public class Notification {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(
            name = "event_id",
            nullable = false,
            length = 100
    )
    private String eventId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "notification_type",
            nullable = false,
            length = 50
    )
    private String notificationType;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "message",
            nullable = false,
            length = 1000
    )
    private String message;

    @Column(
            name = "read_flag",
            nullable = false
    )
    private Boolean readFlag = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (readFlag == null) {
            readFlag = false;
        }

        if (createdAt == null) {
            createdAt =
                    LocalDateTime.now();
        }
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(
            Long notificationId
    ) {
        this.notificationId =
                notificationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(
            String eventId
    ) {
        this.eventId =
                eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(
            Long userId
    ) {
        this.userId =
                userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(
            String notificationType
    ) {
        this.notificationType =
                notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title =
                title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message =
                message;
    }

    public Boolean getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(
            Boolean readFlag
    ) {
        this.readFlag =
                readFlag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }
}