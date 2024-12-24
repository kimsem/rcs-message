package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessagePublisher {

   private final EventHubProducerClient producerClient;
   private final ObjectMapper objectMapper;

   public void publishMessageSendEvent(MessageSendEvent event) {
       try {
           String eventData = objectMapper.writeValueAsString(event);
           producerClient.send(EventData.create(eventData.getBytes()));
           log.info("Published message send event: {}", event.getMessageId());
       } catch (Exception e) {
           log.error("Error publishing message send event", e);
           throw new RuntimeException("Failed to publish message send event", e);
       }
   }

   public void publishUploadEvent(RecipientUploadEvent event) {
       try {
           String eventData = objectMapper.writeValueAsString(event);
           producerClient.send(EventData.create(eventData.getBytes()));
           log.info("Published recipient upload event: {}", event.getMessageGroupId());
       } catch (Exception e) {
           log.error("Error publishing recipient upload event", e);
           throw new RuntimeException("Failed to publish recipient upload event", e);
       }
   }
}
