package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import io.micrometer.core.instrument.Timer;
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
    private final Timer eventHubPublishTimer;  // Timer 주입 추가

    /**
     * 수신자 업로드 이벤트를 발행합니다.
     *
     * @param events 발행할 수신자 업로드 이벤트
     * @throws RuntimeException 이벤트 발행 실패 시
     */
    public void publishUploadEvent(List<RecipientUploadEvent> events) {
        Timer.Sample sample = Timer.start();  // 타이머 시작

        CompletableFuture.runAsync(() -> {
            try {
                String eventData = objectMapper.writeValueAsString(events); // 리스트 전체를 JSON 배열로 직렬화
                EventData batchEventData = new EventData(eventData.getBytes());
                numberEncryptProducerClient.send(Collections.singletonList(batchEventData));
                log.info("Published {} recipient upload events", events.size());
                sample.stop(eventHubPublishTimer);  // 성공시 타이머 정지
            } catch (Exception e) {
                sample.stop(eventHubPublishTimer);  // 실패시에도 타이머 정지
                log.error("Error publishing recipient upload events", e);
                throw new RuntimeException("Failed to publish recipient upload events", e);
            }
        }, executorService); // 스레드 풀을 활용
    }

    public void publishSendEvent(List<MessageSendEvent> events) {
        Timer.Sample sample = Timer.start();  // 타이머 시작
        try {
            String eventData = objectMapper.writeValueAsString(events);
            EventData eventDataObj = new EventData(eventData.getBytes());
            messageSendProducerClient.send(Collections.singletonList(eventDataObj));
            log.info("Published {} recipient send events", events.size());

            sample.stop(eventHubPublishTimer);  // 성공시 타이머 정지
        } catch (Exception e) {
            sample.stop(eventHubPublishTimer);  // 실패시에도 타이머 정지
            log.error("Error publishing message send event", e);
            throw new RuntimeException("Failed to publish message send event", e);
        }
    }
}