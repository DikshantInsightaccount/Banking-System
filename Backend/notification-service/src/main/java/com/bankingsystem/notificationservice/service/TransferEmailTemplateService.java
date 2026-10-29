package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.dto.TransferCompletedEvent;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class TransferEmailTemplateService {

    private static final DateTimeFormatter
            DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );

    public String senderSubject(
            TransferCompletedEvent event
    ) {
        return "Transfer successful - Ref " +
                event.transferId();
    }

    public String recipientSubject(
            TransferCompletedEvent event
    ) {
        return "Money received - Ref " +
                event.transferId();
    }

    public String senderBody(
            TransferCompletedEvent event
    ) {
        return """
                Dear Customer,

                Your money transfer has been completed successfully.

                Amount: %s %s
                From account: %s
                To account: %s
                Reference: %s
                Status: %s
                Transaction time: %s

                If you did not authorize this transaction, contact bank support immediately.

                Thank you,
                Secure Banking System
                """.formatted(
                event.currency(),
                event.amount(),
                maskAccountNumber(
                        event.fromAccountNumber()
                ),
                maskAccountNumber(
                        event.toAccountNumber()
                ),
                event.transferId(),
                event.status(),
                formatTimestamp(event)
        );
    }

    public String recipientBody(
            TransferCompletedEvent event
    ) {
        return """
                Dear Customer,

                Money has been credited to your account.

                Amount: %s %s
                Credited account: %s
                Sender account: %s
                Reference: %s
                Status: %s
                Transaction time: %s

                Thank you,
                Secure Banking System
                """.formatted(
                event.currency(),
                event.amount(),
                maskAccountNumber(
                        event.toAccountNumber()
                ),
                maskAccountNumber(
                        event.fromAccountNumber()
                ),
                event.transferId(),
                event.status(),
                formatTimestamp(event)
        );
    }

    private String formatTimestamp(
            TransferCompletedEvent event
    ) {
        if (event.occurredAt() == null) {
            return "Not available";
        }

        return event
                .occurredAt()
                .format(
                        DATE_TIME_FORMATTER
                );
    }

    private String maskAccountNumber(
            String accountNumber
    ) {
        if (accountNumber == null ||
                accountNumber.length() <= 4) {

            return "****";
        }

        return "****" +
                accountNumber.substring(
                        accountNumber.length() - 4
                );
    }
}