package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessageSubscriber {

    private final EventHubConsumerAsyncClient consumerClient; // 비동기 클라이언트 사용
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @PostConstruct
    public void subscribe() throws InterruptedException {
        startMessageResultSubscription();
    }

    public void startMessageResultSubscription() throws InterruptedException {
        try {
            consumerClient.receiveFromPartition(
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
                            messageService.processMessageResult(event.getMessageId(), event.getStatus());
                        } catch (Exception e) {
                            log.error("Error processing message result event", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error starting event subscription", e);
            // 일정 시간 후 재시도
            Thread.sleep(5000);
            startMessageResultSubscription();
        }
    }
}
