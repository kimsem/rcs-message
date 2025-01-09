package com.ktds.rcsp.history.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.service.HistoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubHistorySubscriber {
    private final EventHubConsumerAsyncClient messageSendHistoryConsumer;
    private final EventHubConsumerAsyncClient messageResultHistoryConsumer;
    private final ObjectMapper objectMapper;
    private final HistoryService historyService;

    @PostConstruct
    public void subscribe() throws InterruptedException {
        sendMessageSubscription();
        resultMessageSubscription();
    }

    private void sendMessageSubscription() throws InterruptedException {
        int retryCount = 0;
        int maxRetries = 5;
        int retryDelay = 5000;

        while (retryCount < maxRetries) {
            try {
                messageSendHistoryConsumer.receiveFromPartition(
                                "0",
                                EventPosition.latest()
                        )
                        .bufferTimeout(100, Duration.ofSeconds(1)) // Batch size of 100 or 1 second timeout
                        .subscribe(partitionEvents -> {
                            List<PartitionEvent> batch = new ArrayList<>();
                            for (PartitionEvent partitionEvent : partitionEvents) {
                                batch.add(partitionEvent);
                                if (batch.size() >= 100) {
                                    processMessageEventBatch(batch);
                                    batch.clear();
                                }
                            }
                            if (!batch.isEmpty()) {
                                processMessageEventBatch(batch);
                            }
                        });
                break;
            } catch (Exception e) {
                log.error("Error starting message event subscription", e);
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

    private void processMessageEventBatch(List<PartitionEvent> partitionEvents) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (PartitionEvent partitionEvent : partitionEvents) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    EventData eventData = partitionEvent.getData();
                    String eventBody = eventData.getBodyAsString();
                    List<MessageSendEvent> eventsToProcess = objectMapper.readValue(
                            eventBody,
                            new TypeReference<List<MessageSendEvent>>() {}
                    );
                    log.info("Processing batch of {} message send events", eventsToProcess.size());
                    historyService.saveMessageHistory(eventsToProcess);
                } catch (Exception e) {
                    log.error("Error processing message send events batch", e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void resultMessageSubscription() throws InterruptedException {
        try {
            messageResultHistoryConsumer.receiveFromPartition(
                            "0", // 파티션 ID
                            EventPosition.latest() // 가장 최신 이벤트부터 수신
                    )
                    .subscribe(partitionEvent -> {
                        try {
                            // 이벤트 데이터 처리
                            EventData eventData = partitionEvent.getData();
                            String eventBody = eventData.getBodyAsString(); // getBodyAsString() 사용
                            log.info(eventBody);

                            MessageResultEvent event = objectMapper.readValue(eventBody, MessageResultEvent.class);

                            log.info("Received message result event: {}", event.getMessageId());
                            historyService.updateMessageStatus(event);
                        } catch (Exception e) {
                            log.error("Error processing message result event", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error starting event subscription", e);
            // 일정 시간 후 재시도
            Thread.sleep(5000);
            resultMessageSubscription();
        }
    }
}
