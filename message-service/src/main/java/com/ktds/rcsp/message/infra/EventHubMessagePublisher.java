package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class EventHubMessagePublisher {

    private final EventHubProducerClient encryptProducerClient;
    private final EventHubProducerClient sendProducerClient;
    private final ObjectMapper objectMapper;

    // 생성자 직접 작성
    public EventHubMessagePublisher(
            @Qualifier("encryptEventHubProducer") EventHubProducerClient encryptProducerClient,
            @Qualifier("sendEventHubProducer") EventHubProducerClient sendProducerClient,
            ObjectMapper objectMapper) {
        this.encryptProducerClient = encryptProducerClient;
        this.sendProducerClient = sendProducerClient;
        this.objectMapper = objectMapper;
    }

    public void publishMessageSendEvent(MessageSendEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            EventData data = new EventData(eventData);
            EventDataBatch batch = sendProducerClient.createBatch();

            if (batch.tryAdd(data)) {
                sendProducerClient.send(batch);
                log.info("Published message send event: {}", event.getMessageId());
            } else {
                log.error("Message send event too large to fit in batch");
                throw new RuntimeException("Message send event too large to fit in batch");
            }
        } catch (Exception e) {
            log.error("Error publishing message send event", e);
            throw new RuntimeException("Failed to publish message send event", e);
        }
    }

    public void publishUploadEvent(RecipientUploadEvent event) {
        try {
            log.debug("Attempting to publish event: {}", event);
            String eventData = objectMapper.writeValueAsString(event);
            EventData data = new EventData(eventData);
            EventDataBatch batch = encryptProducerClient.createBatch();

            if (batch.tryAdd(data)) {
                encryptProducerClient.send(batch);
                log.info("Successfully published event for messageGroupId: {}",
                        event.getMessageGroupId());
            } else {
                log.error("Event too large to fit in batch");
                throw new RuntimeException("Event too large to fit in batch");
            }
        } catch (Exception e) {
            log.error("Failed to publish upload event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish upload event", e);
        }
    }
}