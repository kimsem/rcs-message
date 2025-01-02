package com.ktds.rcsp.message.infra;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.*;
import com.ktds.rcsp.message.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Component
public class EventHubMessageSubscriber {
    private final EventHubConsumerClient encryptConsumerClient;
    private final ObjectMapper objectMapper;
    private final RecipientRepository recipientRepository;
    private final EncryptionService encryptionService;
    private final MessageGroupSummaryRepository messageGroupSummaryRepository;
    private final ConcurrentHashMap<String, Long> lastProcessedOffsets = new ConcurrentHashMap<>();

    private final Map<String, ProcessingStatus> statusMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> processedCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> successCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failCountMap = new ConcurrentHashMap<>();


    public EventHubMessageSubscriber(
            @Qualifier("encryptEventHubConsumer") EventHubConsumerClient encryptConsumerClient,
            ObjectMapper objectMapper,
            RecipientRepository recipientRepository,
            EncryptionService encryptionService,
            MessageGroupSummaryRepository messageGroupSummaryRepository) {
        this.encryptConsumerClient = encryptConsumerClient;
        this.objectMapper = objectMapper;
        this.recipientRepository = recipientRepository;
        this.encryptionService = encryptionService;
        this.messageGroupSummaryRepository = messageGroupSummaryRepository;
    }

    @PostConstruct
    public void subscribe() {
        startEncryptSubscription();
    }

    private void startEncryptSubscription() {
        Thread subscriberThread = new Thread(() -> {
            while (true) {
                try {
                    for (String partitionId : encryptConsumerClient.getPartitionIds()) {
                        Long lastOffset = lastProcessedOffsets.get(partitionId);
                        EventPosition startPosition = (lastOffset != null) ?
                                EventPosition.fromOffset(lastOffset) : // Long 타입 사용
                                EventPosition.latest();

                        Iterable<PartitionEvent> events = encryptConsumerClient.receiveFromPartition(
                                partitionId,
                                100,
                                startPosition,
                                Duration.ofSeconds(10)
                        );

                        for (PartitionEvent partitionEvent : events) {
                            try {
                                processEvent(partitionEvent);
                                // 성공적으로 처리된 메시지의 offset 저장
                                lastProcessedOffsets.put(partitionId,
                                        partitionEvent.getData().getOffset());
                            } catch (Exception e) {
                                log.error("Error processing event", e);
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Error in subscription", e);
                    handleSubscriptionError();
                }
            }
        }, "EncryptSubscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void processEvent(PartitionEvent partitionEvent)  {
        EventData eventData = partitionEvent.getData();
        String eventBody = new String(eventData.getBody());
        RecipientUploadEvent event = objectMapper.readValue(eventBody, RecipientUploadEvent.class);

        if (!isValidEvent(event)) {
            log.warn("Invalid event detected, skipping");
            return;
        }

        Optional<MessageGroupSummary> messageGroupOptional = messageGroupSummaryRepository.findById(event.getMessageGroupId());

        if (messageGroupOptional.isEmpty()) {
            log.warn("Message group not found: {}", event.getMessageGroupId());
            return;
        }

        MessageGroupSummary messageGroup = messageGroupOptional.get();

        String encryptedPhone = encryptionService.encrypt(event.getPhoneNumber());

        // 중복 체크
        if (recipientRepository.existsByMessageGroupIdAndEncryptedPhone(event.getMessageGroupId(), encryptedPhone)) {
            log.info("Duplicate recipient ignored: {} {}", event.getMessageGroupId(), event.getPhoneNumber());
            return;
        }

        // 신규 수신자 저장
        Recipient recipient = Recipient.builder()
                .messageGroupId(event.getMessageGroupId())
                .encryptedPhone(encryptedPhone)
                .status(ProcessingStatus.COMPLETED)
                .build();

        recipientRepository.save(recipient);

        // 처리 건수 업데이트
        int processedCount = messageGroup.getProcessedCount() + 1;
        messageGroup.setProcessedCount(processedCount);

        ProcessingStatus status;
        if (processedCount == 0) {
            status = ProcessingStatus.UPLOADING;
        } else if (processedCount == messageGroup.getTotalCount()) {
            status = ProcessingStatus.COMPLETED;
        } else if (processedCount < messageGroup.getTotalCount()) {
            status = ProcessingStatus.PROCESSING;
        } else {
            status = ProcessingStatus.FAILED;
        }


        messageGroup.setStatus(status.name());
        messageGroup.setProcessedCount(processedCount);
        messageGroup.setUpdatedAt(LocalDateTime.now());

        messageGroupSummaryRepository.save(messageGroup);

        // 상태 맵 업데이트
        statusMap.put(messageGroup.getMessageGroupId(), status);
        processedCountMap.get(messageGroup.getMessageGroupId()).incrementAndGet();
        successCountMap.get(messageGroup.getMessageGroupId()).incrementAndGet();



        try {
            failCountMap.putIfAbsent(messageGroup.getMessageGroupId(), new AtomicInteger(0));
            failCountMap.get(messageGroup.getMessageGroupId()).set(messageGroup.getTotalCount() - processedCount);
        } catch (Exception e) {
            log.error("Error processing event", e);
            failCountMap.putIfAbsent(messageGroup.getMessageGroupId(), new AtomicInteger(0));
            failCountMap.get(messageGroup.getMessageGroupId()).incrementAndGet();
            throw e;
        }
    }


//        // 총 건수와 처리 건수 일치 시 상태 변경
//        long totalRecipients = recipientRepository.countByMessageGroupId(group.getMessageGroupId());
//        long processedRecipients = recipientRepository.countByMessageGroupIdAndStatus(
//                group.getMessageGroupId(), ProcessingStatus.COMPLETED);
//
//        if (totalRecipients > 0 && totalRecipients == processedRecipients) {
//            group.setStatus("COMPLETED");
//            group.setUpdatedAt(LocalDateTime.now());
//            log.info("메시지 그룹 {} 처리 완료", group.getMessageGroupId());
//        }
//
//        messageGroupSummaryRepository.save(group);
//
//        log.debug("이벤트 성공적으로 처리: {} {}",
//                event.getMessageGroupId(), event.getPhoneNumber());
    private boolean isValidEvent(RecipientUploadEvent event) {
        return event != null &&
                event.getMessageGroupId() != null &&
                event.getPhoneNumber() != null;
    }

    private void handleSubscriptionError() {
        try {
            Thread.sleep(5000); // 에러 발생시 5초 대기
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}