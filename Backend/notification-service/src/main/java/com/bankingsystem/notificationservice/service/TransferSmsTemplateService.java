package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.dto.TransferCompletedEvent;

import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class TransferSmsTemplateService {

    public String senderMessage(
            TransferCompletedEvent event
    ) {
        return """
                Secure Banking: %s debited from A/C %s and sent to A/C %s. Ref: %s. If unauthorized, contact support immediately.
                """.formatted(
                formatCurrency(
                        event.amount()
                ),
                maskAccountNumber(
                        event.fromAccountNumber()
                ),
                maskAccountNumber(
                        event.toAccountNumber()
                ),
                event.transferId()
        ).trim();
    }

    public String recipientMessage(
            TransferCompletedEvent event
    ) {
        return """
                Secure Banking: %s credited to A/C %s from A/C %s. Ref: %s.
                """.formatted(
                formatCurrency(
                        event.amount()
                ),
                maskAccountNumber(
                        event.toAccountNumber()
                ),
                maskAccountNumber(
                        event.fromAccountNumber()
                ),
                event.transferId()
        ).trim();
    }

    private String formatCurrency(
            java.math.BigDecimal amount
    ) {
        NumberFormat currencyFormat =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag(
                                "en-IN"
                        )
                );

        return currencyFormat.format(
                amount
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