package com.ktds.rcsp.history.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.service.HistoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    }

    private void sendMessageSubscription() throws InterruptedException {
        try {
            messageSendHistoryConsumer.receiveFromPartition(
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
                            historyService.saveMessageHistory(event);
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
