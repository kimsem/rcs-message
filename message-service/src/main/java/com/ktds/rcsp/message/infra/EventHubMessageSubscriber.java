package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.EventPosition;
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
        try {
            numberEncryptConsumerAsyncClient.receiveFromPartition(
                            "0", // 파티션 ID (예시로 "0"을 사용)
                            EventPosition.latest()
                    )
                    .subscribe(partitionEvent -> {
                        try {
                            // 이벤트 데이터 처리
                            EventData eventData = partitionEvent.getData();
                            String eventBody = eventData.getBodyAsString();
                            RecipientUploadEvent event = objectMapper.readValue(eventBody, RecipientUploadEvent.class);

                            log.info("Received recipient upload event: {}", event.getMessageGroupId());
                            recipientService.encryptAndSaveRecipient(event.getMessageGroupId(), event.getPhoneNumber());
                        } catch (Exception e) {
                            log.error("Error processing recipient upload event", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error starting upload event subscription", e);
            // 일정 시간 후 재시도
            Thread.sleep(5000);
            startUploadEventSubscription();
        }
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
