package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.repository.RecipientRepository;
import com.ktds.rcsp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class EventHubMessageSubscriber {
    private final EventHubConsumerClient encryptConsumerClient;
    private final EventHubConsumerClient sendConsumerClient;
    private final EventHubConsumerClient resultConsumerClient;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final RecipientRepository recipientRepository;
    private final EncryptionService encryptionService;

    public EventHubMessageSubscriber(
            @Qualifier("encryptEventHubConsumer") EventHubConsumerClient encryptConsumerClient,
            @Qualifier("sendEventHubConsumer") EventHubConsumerClient sendConsumerClient,
            @Qualifier("resultEventHubConsumer") EventHubConsumerClient resultConsumerClient,
            ObjectMapper objectMapper,
            MessageService messageService,
            RecipientRepository recipientRepository,
            EncryptionService encryptionService) {
        this.encryptConsumerClient = encryptConsumerClient;
        this.sendConsumerClient = sendConsumerClient;
        this.resultConsumerClient = resultConsumerClient;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.recipientRepository = recipientRepository;
        this.encryptionService = encryptionService;
    }

    @PostConstruct
    public void subscribe() {
        startEncryptSubscription();
        startSendSubscription();
        startResultSubscription();
    }

    private void startEncryptSubscription() {
        Thread subscriberThread = new Thread(() -> {
            while (true) {
                try {
                    for (String partitionId : encryptConsumerClient.getPartitionIds()) {
                        Iterable<PartitionEvent> events = encryptConsumerClient.receiveFromPartition(
                                partitionId,
                                100,
                                EventPosition.latest()
                        );

                        for (PartitionEvent partitionEvent : events) {
                            try {
                                EventData eventData = partitionEvent.getData();
                                String eventBody = new String(eventData.getBody());
                                RecipientUploadEvent event = objectMapper.readValue(eventBody, RecipientUploadEvent.class);

                                processRecipientEvent(event);
                            } catch (Exception e) {
                                log.error("Error processing encrypt event", e);
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Error in encrypt subscription", e);
                    handleSubscriptionError();
                }
            }
        }, "EncryptSubscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void startSendSubscription() {
        Thread subscriberThread = new Thread(() -> {
            while (true) {
                try {
                    for (String partitionId : sendConsumerClient.getPartitionIds()) {
                        Iterable<PartitionEvent> events = sendConsumerClient.receiveFromPartition(
                                partitionId,
                                100,
                                EventPosition.latest()
                        );

                        for (PartitionEvent partitionEvent : events) {
                            try {
                                EventData eventData = partitionEvent.getData();
                                String eventBody = new String(eventData.getBody());
                                MessageSendEvent event = objectMapper.readValue(eventBody, MessageSendEvent.class);

                                processMessageSendEvent(event);
                            } catch (Exception e) {
                                log.error("Error processing send event", e);
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Error in send subscription", e);
                    handleSubscriptionError();
                }
            }
        }, "SendSubscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void startResultSubscription() {
        Thread subscriberThread = new Thread(() -> {
            while (true) {
                try {
                    for (String partitionId : resultConsumerClient.getPartitionIds()) {
                        Iterable<PartitionEvent> events = resultConsumerClient.receiveFromPartition(
                                partitionId,
                                100,
                                EventPosition.latest()
                        );

                        for (PartitionEvent partitionEvent : events) {
                            try {
                                EventData eventData = partitionEvent.getData();
                                String eventBody = new String(eventData.getBody());
                                MessageResultEvent event = objectMapper.readValue(eventBody, MessageResultEvent.class);

                                processMessageResultEvent(event);
                            } catch (Exception e) {
                                log.error("Error processing result event", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in result subscription", e);
                    handleSubscriptionError();
                }
            }
        }, "ResultSubscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void processRecipientEvent(RecipientUploadEvent event) {
        try {
            Recipient recipient = Recipient.builder()
                    .messageGroupId(event.getMessageGroupId())
                    .encryptedPhone(encryptionService.encrypt(event.getPhoneNumber()))
                    .encryptedName(encryptionService.encrypt(event.getName()))
                    .status(ProcessingStatus.COMPLETED)
                    .build();

            recipientRepository.save(recipient);
            log.info("Processed recipient upload event: messageGroupId={}, phone={}",
                    event.getMessageGroupId(), event.getPhoneNumber());
        } catch (Exception e) {
            log.error("Error processing recipient event", e);
        }
    }

    private void processMessageSendEvent(MessageSendEvent event) {
        try {
            log.info("Processing message send event: messageId={}", event.getMessageId());
            // 메시지 전송 처리 로직 구현
        } catch (Exception e) {
            log.error("Error processing message send event", e);
        }
    }

    private void processMessageResultEvent(MessageResultEvent event) {
        try {
            log.info("Processing message result event: messageId={}, status={}",
                    event.getMessageId(), event.getStatus());
            messageService.processMessageResult(event.getMessageId(), event.getStatus());
        } catch (Exception e) {
            log.error("Error processing message result event", e);
        }
    }

    private void handleSubscriptionError() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}