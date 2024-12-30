package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubMessagePublisher {

   private final EventHubProducerClient producerClient;
   private final ObjectMapper objectMapper;



   public void publishMessageSendEvent(MessageSendEvent event) {
       try {
           EventDataBatch batch = producerClient.createBatch();
           String eventData = objectMapper.writeValueAsString(event);
           // 새로운 EventData 인스턴스 생성
           EventData evt = new EventData(eventData.getBytes());

           if (!batch.tryAdd(evt)) {
               producerClient.send(batch);
               batch = producerClient.createBatch();
               if (!batch.tryAdd(evt)) {
                   throw new IllegalArgumentException("Event is too large for an empty batch");
               }
           }
           producerClient.send(batch);
           log.info("Published message send event: {}", event.getMessageId());
       } catch (Exception e) {
           log.error("Error publishing message send event", e);
           throw new RuntimeException("Failed to publish message send event", e);
       }
   }

    public void publishUploadEvent(RecipientUploadEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            EventData eventDataObj = new EventData(eventData.getBytes());

            // EventData를 List로 감싸서 전송
            List<EventData> events = Collections.singletonList(eventDataObj);
            producerClient.send(events);

            log.info("Published recipient upload event: {}", event.getMessageGroupId());
        } catch (Exception e) {
            log.error("Error publishing recipient upload event", e);
            throw new RuntimeException("Failed to publish recipient upload event", e);
        }
    }
}
