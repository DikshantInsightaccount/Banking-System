package com.transactionservice.transactionservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactionservice.transactionservice.events.TransferCompletedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransferEventKafkaProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    TransferEventKafkaProducer.class
            );

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final String transferCompletedTopic;

    public TransferEventKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,

            @Value(
                    "${banking.kafka.topics.transfer-completed}"
            )
            String transferCompletedTopic
    ) {
        this.kafkaTemplate =
                kafkaTemplate;

        this.objectMapper =
                objectMapper;

        this.transferCompletedTopic =
                transferCompletedTopic;
    }

    /*
     * The Kafka message is sent only after the database
     * transaction commits successfully.
     *
     * If secureTransfer() rolls back, this listener will
     * not publish a TransferCompletedEvent to Kafka.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTransferCompleted(
            TransferCompletedEvent event
    ) {
        try {
            String payload =
                    objectMapper.writeValueAsString(
                            event
                    );

            String messageKey =
                    String.valueOf(
                            event.transferId()
                    );

            kafkaTemplate
                    .send(
                            transferCompletedTopic,
                            messageKey,
                            payload
                    )
                    .whenComplete(
                            (sendResult, exception) -> {
                                if (exception != null) {
                                    LOGGER.error(
                                            "Failed to publish transfer event. " +
                                                    "eventId={}, transferId={}, topic={}",
                                            event.eventId(),
                                            event.transferId(),
                                            transferCompletedTopic,
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Transfer event published successfully. " +
                                                "eventId={}, transferId={}, topic={}, " +
                                                "partition={}, offset={}",
                                        event.eventId(),
                                        event.transferId(),
                                        transferCompletedTopic,
                                        sendResult
                                                .getRecordMetadata()
                                                .partition(),
                                        sendResult
                                                .getRecordMetadata()
                                                .offset()
                                );
                            }
                    );

        } catch (JsonProcessingException exception) {
            LOGGER.error(
                    "Failed to serialize transfer event. " +
                            "eventId={}, transferId={}",
                    event.eventId(),
                    event.transferId(),
                    exception
            );
        }
    }
}