package com.bankingsystem.notificationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        String eventId,
        String eventType,
        Integer eventVersion,

        Long transferId,

        Long fromAccountId,
        String fromAccountNumber,
        Long senderUserId,
        String senderEmail,
        String senderPhone,

        Long toAccountId,
        String toAccountNumber,
        Long recipientUserId,
        String recipientEmail,
        String recipientPhone,

        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime occurredAt
) {
}