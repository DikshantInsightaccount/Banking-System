package com.transactionservice.transactionservice.kafka;

import com.transactionservice.transactionservice.entity.Account;
import com.transactionservice.transactionservice.entity.Customer;
import com.transactionservice.transactionservice.entity.Transfer;
import com.transactionservice.transactionservice.events.TransferCompletedEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransferEventPublisher {

    private static final String EVENT_TYPE =
            "TRANSFER_COMPLETED";

    private static final Integer EVENT_VERSION =
            1;

    private static final String CURRENCY =
            "INR";

    private final ApplicationEventPublisher
            applicationEventPublisher;

    public TransferEventPublisher(
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.applicationEventPublisher =
                applicationEventPublisher;
    }

    public void publishTransferCompleted(
            Transfer transfer
    ) {
        validateTransfer(transfer);

        Account fromAccount =
                transfer.getFromAccount();

        Account toAccount =
                transfer.getToAccount();

        Customer senderCustomer =
                requireCustomer(
                        fromAccount,
                        "Source"
                );

        Customer recipientCustomer =
                requireCustomer(
                        toAccount,
                        "Destination"
                );

        Long senderUserId =
                requireUserId(
                        senderCustomer,
                        "Sender"
                );

        Long recipientUserId =
                requireUserId(
                        recipientCustomer,
                        "Recipient"
                );

        String senderEmail =
                requireEmail(
                        senderCustomer.getEmail(),
                        "Sender"
                );

        String recipientEmail =
                requireEmail(
                        recipientCustomer.getEmail(),
                        "Recipient"
                );

        /*
         * Phone numbers are now included in the Kafka
         * event for SMS delivery.
         */
        String senderPhone =
                requirePhone(
                        senderCustomer.getPhone(),
                        "Sender"
                );

        String recipientPhone =
                requirePhone(
                        recipientCustomer.getPhone(),
                        "Recipient"
                );

        LocalDateTime occurredAt =
                transfer.getTxntimestamp() != null
                        ? transfer.getTxntimestamp()
                        : LocalDateTime.now();

        String status =
                transfer.getStatus() != null
                        ? transfer.getStatus().name()
                        : "SUCCESS";

        TransferCompletedEvent event =
                new TransferCompletedEvent(
                        UUID.randomUUID().toString(),
                        EVENT_TYPE,
                        EVENT_VERSION,

                        transfer.getTransferid(),

                        /*
                         * Sender information
                         */
                        fromAccount.getAccountid(),
                        fromAccount.getAccountnumber(),
                        senderUserId,
                        senderEmail,
                        senderPhone,

                        /*
                         * Recipient information
                         */
                        toAccount.getAccountid(),
                        toAccount.getAccountnumber(),
                        recipientUserId,
                        recipientEmail,
                        recipientPhone,

                        /*
                         * Transfer information
                         */
                        transfer.getAmount(),
                        CURRENCY,
                        status,
                        occurredAt
                );

        applicationEventPublisher.publishEvent(
                event
        );
    }

    private void validateTransfer(
            Transfer transfer
    ) {
        if (transfer == null) {
            throw new IllegalArgumentException(
                    "Transfer is required for event publishing"
            );
        }

        if (transfer.getTransferid() == null) {
            throw new IllegalStateException(
                    "Transfer must be persisted before publishing an event"
            );
        }

        if (transfer.getFromAccount() == null) {
            throw new IllegalStateException(
                    "Source account is required for event publishing"
            );
        }

        if (transfer.getToAccount() == null) {
            throw new IllegalStateException(
                    "Destination account is required for event publishing"
            );
        }

        if (transfer.getAmount() == null ||
                transfer.getAmount().signum() <= 0) {

            throw new IllegalStateException(
                    "Transfer amount must be greater than zero"
            );
        }
    }

    private Customer requireCustomer(
            Account account,
            String accountRole
    ) {
        if (account.getCustomer() == null) {
            throw new IllegalStateException(
                    accountRole +
                            " account customer is unavailable"
            );
        }

        return account.getCustomer();
    }

    private Long requireUserId(
            Customer customer,
            String customerRole
    ) {
        if (customer.getUser() == null ||
                customer.getUser().getUserid() == null) {

            throw new IllegalStateException(
                    customerRole +
                            " user ID is unavailable"
            );
        }

        return customer
                .getUser()
                .getUserid();
    }

    private String requireEmail(
            String email,
            String customerRole
    ) {
        if (email == null ||
                email.isBlank()) {

            throw new IllegalStateException(
                    customerRole +
                            " email address is unavailable"
            );
        }

        return email.trim();
    }

    private String requirePhone(
            String phone,
            String customerRole
    ) {
        if (phone == null ||
                phone.isBlank()) {

            throw new IllegalStateException(
                    customerRole +
                            " phone number is unavailable"
            );
        }

        return phone.trim();
    }
}