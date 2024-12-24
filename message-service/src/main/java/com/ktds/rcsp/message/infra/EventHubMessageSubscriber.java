package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessageSubscriber {

    private final EventHubConsumerClient consumerClient;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @PostConstruct
    public void subscribe() {
        startMessageResultSubscription();
    }

    private void startMessageResultSubscription() {
        consumerClient.receive(false)
                .subscribe(partitionEvent -> {
                    try {
                        EventData eventData = partitionEvent.getData();
                        String eventBody = new String(eventData.getBody());
                        MessageResultEvent event = objectMapper.readValue(eventBody, MessageResultEvent.class);

                        log.info("Received message result event: {}", event.getMessageId());
                        messageService.processMessageResult(event.getMessageId(), event.getStatus());

                    } catch (Exception e) {
                        log.error("Error processing message result event", e);
                    }
                });
    }
}