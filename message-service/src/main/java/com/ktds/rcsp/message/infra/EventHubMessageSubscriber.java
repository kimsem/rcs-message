package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.service.MessageService;
import com.ktds.rcsp.message.service.RecipientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.type.TypeReference;


import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessageSubscriber {

    private final EventHubConsumerAsyncClient numberEncryptConsumerAsyncClient;
    private final EventHubConsumerAsyncClient messageSendMessageConsumer;
    private final EventHubConsumerAsyncClient messageResultMessageConsumer;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final RecipientService recipientService;

    @PostConstruct
    public void subscribe() throws InterruptedException {
        startUploadEventSubscription();
        messageResultSubscription();
        sendMessageSubscription();
    }

    private void startUploadEventSubscription() throws InterruptedException {
        int retryCount = 0;
        int maxRetries = 5;
        int retryDelay = 5000;

        while (retryCount < maxRetries) {
            try {
                numberEncryptConsumerAsyncClient.receiveFromPartition(
                                "0",
                                EventPosition.latest()
                        )
                        .bufferTimeout(100, Duration.ofSeconds(1)) // Batch size of 100 or 1 second timeout
                        .subscribe(partitionEvents -> {
                            List<PartitionEvent> batch = new ArrayList<>();
                            for (PartitionEvent partitionEvent : partitionEvents) {
                                batch.add(partitionEvent);
                                if (batch.size() >= 100) { // Adjust batch size as needed
                                    processBatch(batch);
                                    batch.clear();
                                }
                            }
                            if (!batch.isEmpty()) {
                                processBatch(batch);
                            }
                        });
                break;
            } catch (Exception e) {
                log.error("Error starting upload event subscription", e);
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("Max retries reached. Giving up.");
                    break;
                }
                Thread.sleep(retryDelay);
                retryDelay *= 2;
            }
        }
    }

    private void processBatch(List<PartitionEvent> partitionEvents) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (PartitionEvent partitionEvent : partitionEvents) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    EventData eventData = partitionEvent.getData();
                    String eventBody = eventData.getBodyAsString();
                    List<RecipientUploadEvent> eventsToSave = objectMapper.readValue(
                            eventBody,
                            new TypeReference<List<RecipientUploadEvent>>() {}
                    );
                    log.info("Processing batch of {} recipient upload events", eventsToSave.size());
                    recipientService.encryptAndSaveRecipient(eventsToSave);
                } catch (Exception e) {
                    log.error("Error processing recipient upload events batch", e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public void messageResultSubscription() throws InterruptedException {
        try {
            messageResultMessageConsumer.receiveFromPartition(
                            "0", // 파티션 ID
                            EventPosition.latest() // 가장 최신 이벤트부터 수신
                    )
                    .subscribe(partitionEvent -> {
                        try {
                            // 이벤트 데이터 처리
                            EventData eventData = partitionEvent.getData();
                            String eventBody = eventData.getBodyAsString(); // getBodyAsString() 사용
                            MessageResultEvent event = objectMapper.readValue(eventBody, MessageResultEvent.class);

                            log.info("Received message result event: {}", event.getMessageId());
                            messageService.processMessageResult(event);
                        } catch (Exception e) {
                            log.error("Error processing message result event", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error starting event subscription", e);
            // 일정 시간 후 재시도
            Thread.sleep(5000);
            messageResultSubscription();
        }
    }
    public void sendMessageSubscription() throws InterruptedException {
        try {
            messageSendMessageConsumer.receiveFromPartition(
                            "0", // 파티션 ID
                            EventPosition.latest() // 가장 최신 이벤트부터 수신
                    )
                    .subscribe(partitionEvent -> {
                        try {
                            // 이벤트 데이터 처리
                            EventData eventData = partitionEvent.getData();
                            String eventBody = eventData.getBodyAsString(); // getBodyAsString() 사용
                            MessageSendEvent event = objectMapper.readValue(eventBody, MessageSendEvent.class);

                            log.info("Received message result event: {}", event.getMessageId());
                            messageService.processMessageResultEvent(event);
                        } catch (Exception e) {
                            log.error("Error processing message result event", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error starting event subscription", e);
            // 일정 시간 후 재시도
            Thread.sleep(5000);
            sendMessageSubscription();
        }
    }


}
