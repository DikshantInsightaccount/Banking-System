package com.bankingsystem.notificationservice.kafka;

import com.bankingsystem.notificationservice.dto.TransferCompletedEvent;
import com.bankingsystem.notificationservice.entity.Notification;
import com.bankingsystem.notificationservice.repository.NotificationRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankingsystem.notificationservice.service.EmailNotificationService;
import com.bankingsystem.notificationservice.service.TransferEmailTemplateService;
import com.bankingsystem.notificationservice.service.SmsNotificationService;
import com.bankingsystem.notificationservice.service.TransferSmsTemplateService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

@Component
public class TransferEventConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    TransferEventConsumer.class
            );

    private static final String EXPECTED_EVENT_TYPE =
            "TRANSFER_COMPLETED";

    private static final Integer SUPPORTED_EVENT_VERSION =
            1;

    private static final String SENT_NOTIFICATION =
            "TRANSFER_SENT";

    private static final String RECEIVED_NOTIFICATION =
            "TRANSFER_RECEIVED";

    private final JsonMapper jsonMapper;

    private final NotificationRepository
            notificationRepository;

    private final EmailNotificationService emailNotificationService;

    private final TransferEmailTemplateService transferEmailTemplateService;
    private final SmsNotificationService smsNotificationService;

    private final TransferSmsTemplateService transferSmsTemplateService;

    public TransferEventConsumer(
            JsonMapper jsonMapper,
            NotificationRepository notificationRepository,
            EmailNotificationService emailNotificationService,
            TransferEmailTemplateService transferEmailTemplateService,
            SmsNotificationService smsNotificationService,
            TransferSmsTemplateService transferSmsTemplateService
    ) {
        this.jsonMapper =
                jsonMapper;

        this.notificationRepository =
                notificationRepository;
        this.emailNotificationService = emailNotificationService;
        this.transferEmailTemplateService = transferEmailTemplateService;
        this.smsNotificationService = smsNotificationService;
        this.transferSmsTemplateService = transferSmsTemplateService;
    }

    @KafkaListener(
            topics =
                    "${banking.kafka.topics.transfer-completed}",

            groupId =
                    "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void consumeTransferCompleted(
            ConsumerRecord<String, String> record
    ) {
        LOGGER.debug(
                "Kafka record received. " +
                        "key={}, partition={}, offset={}",
                record.key(),
                record.partition(),
                record.offset()
        );

        TransferCompletedEvent event =
                jsonMapper.readValue(
                        record.value(),
                        TransferCompletedEvent.class
                );

        validateEvent(event);

        LOGGER.info(
                "Transfer event received. " +
                        "eventId={}, transferId={}, " +
                        "partition={}, offset={}",
                event.eventId(),
                event.transferId(),
                record.partition(),
                record.offset()
        );

        createSenderNotification(event);
        createRecipientNotification(event);

        sendTransferEmails(event);
        sendTransferSms(event);
    }

    private void sendTransferSms(
            TransferCompletedEvent event
    ) {
        sendSenderSmsSafely(event);
        sendRecipientSmsSafely(event);
    }

    private void sendSenderSmsSafely(
            TransferCompletedEvent event
    ) {
        try {
            smsNotificationService.sendSms(
                    event.senderPhone(),

                    transferSmsTemplateService
                            .senderMessage(event)
            );

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Sender SMS delivery failed. " +
                            "eventId={}, transferId={}, userId={}",
                    event.eventId(),
                    event.transferId(),
                    event.senderUserId(),
                    exception
            );
        }
    }

    private void sendRecipientSmsSafely(
            TransferCompletedEvent event
    ) {
        try {
            smsNotificationService.sendSms(
                    event.recipientPhone(),

                    transferSmsTemplateService
                            .recipientMessage(event)
            );

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Recipient SMS delivery failed. " +
                            "eventId={}, transferId={}, userId={}",
                    event.eventId(),
                    event.transferId(),
                    event.recipientUserId(),
                    exception
            );
        }
    }

    private void sendTransferEmails(
            TransferCompletedEvent event
    ) {
        sendSenderEmailSafely(event);
        sendRecipientEmailSafely(event);
    }

    private void sendSenderEmailSafely(
            TransferCompletedEvent event
    ) {
        try {
            emailNotificationService.sendEmail(
                    event.senderEmail(),

                    transferEmailTemplateService
                            .senderSubject(event),

                    transferEmailTemplateService
                            .senderBody(event)
            );

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Sender email delivery failed. " +
                            "eventId={}, transferId={}, userId={}",
                    event.eventId(),
                    event.transferId(),
                    event.senderUserId(),
                    exception
            );
        }
    }

    private void sendRecipientEmailSafely(
            TransferCompletedEvent event
    ) {
        try {
            emailNotificationService.sendEmail(
                    event.recipientEmail(),

                    transferEmailTemplateService
                            .recipientSubject(event),

                    transferEmailTemplateService
                            .recipientBody(event)
            );

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Recipient email delivery failed. " +
                            "eventId={}, transferId={}, userId={}",
                    event.eventId(),
                    event.transferId(),
                    event.recipientUserId(),
                    exception
            );
        }
    }

    private void createSenderNotification(
            TransferCompletedEvent event
    ) {
        String message =
                "Transfer successful: " +
                        event.currency() +
                        " " +
                        event.amount() +
                        " transferred from account " +
                        maskAccountNumber(
                                event.fromAccountNumber()
                        ) +
                        " to account " +
                        maskAccountNumber(
                                event.toAccountNumber()
                        ) +
                        ". Reference: " +
                        event.transferId();

        saveNotification(
                event.eventId(),
                event.senderUserId(),
                SENT_NOTIFICATION,
                "Transfer Successful",
                message
        );
    }

    private void createRecipientNotification(
            TransferCompletedEvent event
    ) {
        String message =
                event.currency() +
                        " " +
                        event.amount() +
                        " received in account " +
                        maskAccountNumber(
                                event.toAccountNumber()
                        ) +
                        " from account " +
                        maskAccountNumber(
                                event.fromAccountNumber()
                        ) +
                        ". Reference: " +
                        event.transferId();

        saveNotification(
                event.eventId(),
                event.recipientUserId(),
                RECEIVED_NOTIFICATION,
                "Money Received",
                message
        );
    }

    private void saveNotification(
            String eventId,
            Long userId,
            String notificationType,
            String title,
            String message
    ) {
        boolean alreadyExists =
                notificationRepository
                        .existsByEventIdAndUserIdAndNotificationType(
                                eventId,
                                userId,
                                notificationType
                        );

        if (alreadyExists) {
            LOGGER.info(
                    "Duplicate notification skipped. " +
                            "eventId={}, userId={}, type={}",
                    eventId,
                    userId,
                    notificationType
            );

            return;
        }

        Notification notification =
                new Notification();

        notification.setEventId(
                eventId
        );

        notification.setUserId(
                userId
        );

        notification.setNotificationType(
                notificationType
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setReadFlag(
                false
        );

        try {
            Notification saved =
                    notificationRepository.saveAndFlush(
                            notification
                    );

            LOGGER.info(
                    "Notification stored. " +
                            "notificationId={}, eventId={}, " +
                            "userId={}, type={}",
                    saved.getNotificationId(),
                    eventId,
                    userId,
                    notificationType
            );
        } catch (DataIntegrityViolationException exception) {
            LOGGER.info(
                    "Duplicate notification prevented by database. " +
                            "eventId={}, userId={}, type={}",
                    eventId,
                    userId,
                    notificationType
            );
        }
    }

    private void validateEvent(
            TransferCompletedEvent event
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Transfer event is required"
            );
        }

        if (event.eventId() == null ||
                event.eventId().isBlank()) {

            throw new IllegalArgumentException(
                    "Transfer event ID is required"
            );
        }

        if (!EXPECTED_EVENT_TYPE.equals(
                event.eventType()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported event type: " +
                            event.eventType()
            );
        }

        if (!SUPPORTED_EVENT_VERSION.equals(
                event.eventVersion()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported event version: " +
                            event.eventVersion()
            );
        }

        if (event.transferId() == null) {
            throw new IllegalArgumentException(
                    "Transfer ID is required"
            );
        }

        if (event.senderUserId() == null) {
            throw new IllegalArgumentException(
                    "Sender user ID is required"
            );
        }

        if (event.recipientUserId() == null) {
            throw new IllegalArgumentException(
                    "Recipient user ID is required"
            );
        }

        if (event.amount() == null ||
                event.amount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero"
            );
        }

        if (event.senderEmail() == null ||
                event.senderEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Sender email is required"
            );
        }

        if (event.recipientEmail() == null ||
                event.recipientEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient email is required"
            );
        }

        if (event.senderPhone() == null ||
                event.senderPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Sender phone number is required"
            );
        }

        if (event.recipientPhone() == null ||
                event.recipientPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient phone number is required"
            );
        }
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
