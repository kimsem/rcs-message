package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.MessageGroup;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageGroupRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final MessageGroupRepository messageGroupRepository;
    private final EncryptionService encryptionService;
    private final EventHubMessagePublisher eventPublisher;

    @Async("recipientProcessorExecutor")
    @Override
    public void processRecipientFile(String messageGroupId, MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser csvParser = CSVFormat.Builder.create()
                    .setHeader()
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            // 수신자 정보를 처리할 스레드 수
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            int totalCount = 0;
            for (CSVRecord record : csvParser) {
                String phoneNumber = record.get("phoneNumber");

                // 비동기 처리로 이벤트 발행
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        RecipientUploadEvent event = RecipientUploadEvent.builder()
                                .messageGroupId(messageGroupId)
                                .phoneNumber(phoneNumber)
                                .build();
                        // EventHub에 이벤트 발행
                        eventPublisher.publishUploadEvent(event);
                    } catch (Exception e) {
                        log.error("Error processing recipient with phone number: {}", phoneNumber, e);
                    }
                });

                futures.add(future);
            }

            // 모든 비동기 작업 완료 기다리기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 처리된 수신자 수 로깅
            log.info("Processed {} recipients for message group: {}", totalCount, messageGroupId);

        } catch (Exception e) {
            log.error("Error processing recipient file", e);
            throw new RuntimeException("Failed to process recipient file", e);
        }
    }

    @Override
    @Transactional
    public void encryptAndSaveRecipient(String messageGroupId, String phoneNumber) {
        try {
            String encryptedPhoneNumber = encryptionService.encrypt(phoneNumber);
            saveRecipient(messageGroupId, encryptedPhoneNumber, ProcessingStatus.COMPLETED, null, null);
        } catch (Exception e) {
            log.error("Failed to encrypt and save recipient", e);
            saveRecipient(messageGroupId, phoneNumber, ProcessingStatus.FAILED, "20000", "encryption failed");
            throw new RuntimeException("Failed to encrypt and save recipient", e);
        }
    }

    private void saveRecipient(String messageGroupId, String phoneNumber, ProcessingStatus status, String errorCode, String errorMessage) {
        MessageGroup messageGroup = messageGroupRepository.findById(messageGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid messageGroupId: " + messageGroupId));

        Recipient recipient = Recipient.builder()
                .messageGroup(messageGroup)
                .encryptedPhone(status == ProcessingStatus.COMPLETED ? phoneNumber : null)
                .status(status)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();

        recipientRepository.save(recipient);

    }
}