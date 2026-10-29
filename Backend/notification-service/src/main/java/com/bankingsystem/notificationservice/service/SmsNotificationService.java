package com.bankingsystem.notificationservice.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    SmsNotificationService.class
            );

    private final boolean smsEnabled;

    private final String fromNumber;

    private final PhoneNumberService
            phoneNumberService;

    public SmsNotificationService(
            PhoneNumberService phoneNumberService,

            @Value(
                    "${banking.sms.enabled:false}"
            )
            boolean smsEnabled,

            @Value(
                    "${banking.sms.account-sid:}"
            )
            String accountSid,

            @Value(
                    "${banking.sms.auth-token:}"
            )
            String authToken,

            @Value(
                    "${banking.sms.from-number:}"
            )
            String fromNumber
    ) {
        this.phoneNumberService =
                phoneNumberService;

        this.smsEnabled =
                smsEnabled;

        if (!smsEnabled) {
            this.fromNumber =
                    null;

            LOGGER.info(
                    "SMS delivery is disabled"
            );

            return;
        }

        validateConfiguration(
                accountSid,
                authToken,
                fromNumber
        );

        this.fromNumber =
                phoneNumberService.normalize(
                        fromNumber
                );

        Twilio.init(
                accountSid.trim(),
                authToken.trim()
        );

        LOGGER.info(
                "Twilio SMS client initialized. " +
                        "fromNumber={}",
                phoneNumberService.mask(
                        this.fromNumber
                )
        );
    }

    public String sendSms(
            String recipient,
            String body
    ) {
        validateMessage(
                recipient,
                body
        );

        String normalizedRecipient =
                phoneNumberService.normalize(
                        recipient
                );

        if (!smsEnabled) {
            LOGGER.info(
                    "SMS delivery disabled. " +
                            "recipient={}",
                    phoneNumberService.mask(
                            normalizedRecipient
                    )
            );

            return null;
        }

        try {
            Message message =
                    Message.creator(
                            new PhoneNumber(
                                    normalizedRecipient
                            ),
                            new PhoneNumber(
                                    fromNumber
                            ),
                            body
                    ).create();

            LOGGER.info(
                    "SMS accepted by Twilio. " +
                            "messageSid={}, recipient={}, status={}",
                    message.getSid(),
                    phoneNumberService.mask(
                            normalizedRecipient
                    ),
                    message.getStatus()
            );

            return message.getSid();

        } catch (ApiException exception) {
            LOGGER.error(
                    "Twilio rejected SMS delivery. " +
                            "recipient={}, code={}, statusCode={}",
                    phoneNumberService.mask(
                            normalizedRecipient
                    ),
                    exception.getCode(),
                    exception.getStatusCode(),
                    exception
            );

            throw exception;

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "SMS delivery failed. recipient={}",
                    phoneNumberService.mask(
                            normalizedRecipient
                    ),
                    exception
            );

            throw exception;
        }
    }

    private void validateConfiguration(
            String accountSid,
            String authToken,
            String configuredFromNumber
    ) {
        if (accountSid == null ||
                accountSid.isBlank()) {

            throw new IllegalStateException(
                    "Twilio Account SID is required " +
                            "when SMS is enabled"
            );
        }

        if (!accountSid.trim().startsWith("AC")) {
            throw new IllegalStateException(
                    "Twilio Account SID is invalid"
            );
        }

        if (authToken == null ||
                authToken.isBlank()) {

            throw new IllegalStateException(
                    "Twilio Auth Token is required " +
                            "when SMS is enabled"
            );
        }

        if (configuredFromNumber == null ||
                configuredFromNumber.isBlank()) {

            throw new IllegalStateException(
                    "Twilio sender number is required " +
                            "when SMS is enabled"
            );
        }
    }

    private void validateMessage(
            String recipient,
            String body
    ) {
        if (recipient == null ||
                recipient.isBlank()) {

            throw new IllegalArgumentException(
                    "SMS recipient is required"
            );
        }

        if (body == null ||
                body.isBlank()) {

            throw new IllegalArgumentException(
                    "SMS message is required"
            );
        }

        if (body.length() > 500) {
            throw new IllegalArgumentException(
                    "SMS message is too long"
            );
        }
    }
}