package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Azure Event Hub를 통해 이벤트를 발행하는 Publisher 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessagePublisher {

    private final EventHubProducerClient numberEncryptProducerClient;
    private final EventHubProducerClient messageSendProducerClient;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    /**
     * 수신자 업로드 이벤트를 발행합니다.
     *
     * @param event 발행할 수신자 업로드 이벤트
     * @throws RuntimeException 이벤트 발행 실패 시
     */
    public void publishUploadEvent(RecipientUploadEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                String eventData = objectMapper.writeValueAsString(event);
                EventData eventDataObj = new EventData(eventData.getBytes());
                numberEncryptProducerClient.send(Collections.singletonList(eventDataObj));

                log.info("Published recipient upload event: {}", event.getMessageGroupId());
            } catch (Exception e) {
                log.error("Error publishing recipient upload event", e);
                throw new RuntimeException("Failed to publish recipient upload event", e);
            }
        }, executorService); // executorService를 사용하여 비동기적으로 발행 작업을 실행
    }

    public void publishSendEvent(MessageSendEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            EventData eventDataObj = new EventData(eventData.getBytes());
            messageSendProducerClient.send(Collections.singletonList(eventDataObj));
            log.info("Published message send event: {}", event.getMessageId());
        } catch (Exception e) {
            log.error("Error publishing message send event", e);
            throw new RuntimeException("Failed to publish message send event", e);
        }
    }
}