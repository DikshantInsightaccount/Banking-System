package com.bankingsystem.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailNotificationService.class
            );

    private final JavaMailSender mailSender;

    private final String fromAddress;

    private final boolean mailEnabled;

    public EmailNotificationService(
            JavaMailSender mailSender,

            @Value("${banking.mail.from}")
            String fromAddress,

            @Value("${banking.mail.enabled:true}")
            boolean mailEnabled
    ) {
        this.mailSender =
                mailSender;

        this.fromAddress =
                fromAddress;

        this.mailEnabled =
                mailEnabled;
    }

    public void sendEmail(
            String recipient,
            String subject,
            String body
    ) {
        validateEmailRequest(
                recipient,
                subject,
                body
        );

        if (!mailEnabled) {
            LOGGER.info(
                    "Email delivery disabled. " +
                            "recipient={}, subject={}",
                    maskEmail(recipient),
                    subject
            );

            return;
        }

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                fromAddress
        );

        message.setTo(
                recipient
        );

        message.setSubject(
                subject
        );

        message.setText(
                body
        );

        try {
            mailSender.send(
                    message
            );

            LOGGER.info(
                    "Email sent successfully. " +
                            "recipient={}, subject={}",
                    maskEmail(recipient),
                    subject
            );
        } catch (MailException exception) {
            LOGGER.error(
                    "Email delivery failed. " +
                            "recipient={}, subject={}",
                    maskEmail(recipient),
                    subject,
                    exception
            );

            throw exception;
        }
    }

    private void validateEmailRequest(
            String recipient,
            String subject,
            String body
    ) {
        if (recipient == null ||
                recipient.isBlank()) {

            throw new IllegalArgumentException(
                    "Email recipient is required"
            );
        }

        if (!recipient.contains("@")) {
            throw new IllegalArgumentException(
                    "Email recipient is invalid"
            );
        }

        if (subject == null ||
                subject.isBlank()) {

            throw new IllegalArgumentException(
                    "Email subject is required"
            );
        }

        if (body == null ||
                body.isBlank()) {

            throw new IllegalArgumentException(
                    "Email body is required"
            );
        }
    }

    private String maskEmail(
            String email
    ) {
        if (email == null ||
                email.isBlank() ||
                !email.contains("@")) {

            return "****";
        }

        int separatorIndex =
                email.indexOf('@');

        String localPart =
                email.substring(
                        0,
                        separatorIndex
                );

        String domain =
                email.substring(
                        separatorIndex
                );

        if (localPart.length() <= 2) {
            return "**" + domain;
        }

        return localPart.substring(0, 2) +
                "****" +
                domain;
    }
}