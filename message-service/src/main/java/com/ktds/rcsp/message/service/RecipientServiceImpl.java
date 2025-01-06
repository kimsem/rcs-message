package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.MessageGroup;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.dto.RecipientResponse;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageGroupRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final MessageGroupRepository messageGroupRepository;
    private final EncryptionService encryptionService;
    private final EventHubMessagePublisher eventPublisher;
    private final ExecutorService executorService;
    private final EntityManager entityManager;
    private static final int BATCH_SIZE = 1000;

    @Async
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

            List<RecipientUploadEvent> eventBatch = new ArrayList<>(BATCH_SIZE);

            for (CSVRecord record : csvParser) {
                String phoneNumber = record.get("phoneNumber");
                eventBatch.add(RecipientUploadEvent.builder()
                        .messageGroupId(messageGroupId)
                        .phoneNumber(phoneNumber)
                        .build());

                // 1000개씩 이벤트 발행
                if (eventBatch.size() >= 1000) {
                    eventPublisher.publishUploadEvent(eventBatch);
                    eventBatch = new ArrayList<>(BATCH_SIZE);
                }
            }

            // 남은 배치 처리
            if (!eventBatch.isEmpty()) {
                eventPublisher.publishUploadEvent(eventBatch);
            }

        } catch (Exception e) {
            log.error("Error processing recipient file", e);
            throw new RuntimeException("Failed to process recipient file", e);
        }
    }

    private void saveRecipientBatch(List<Recipient> recipients) {
        try {
            recipientRepository.saveAll(recipients);
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            log.error("Failed to save recipient batch", e);
            throw new RuntimeException("Failed to save recipient batch", e);
        }
    }

    @Transactional
    @Override
    public void encryptAndSaveRecipient(List<RecipientUploadEvent> events) {
        List<Recipient> recipientsToSave = new ArrayList<>();
        Map<String, MessageGroup> messageGroupCache = new HashMap<>();

        for (RecipientUploadEvent event : events) {
            String messageGroupId = event.getMessageGroupId();
            MessageGroup messageGroup = messageGroupCache.computeIfAbsent(messageGroupId, id ->
                    messageGroupRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid messageGroupId: " + id))
            );
            try {
                String encryptedPhoneNumber = encryptionService.encrypt(event.getPhoneNumber());
                recipientsToSave.add(Recipient.builder().messageGroup(messageGroup).encryptedPhone(encryptedPhoneNumber).status(ProcessingStatus.COMPLETED).build());
            } catch (Exception e) {
                log.error("Failed to encrypt recipient with MessageGroupId: {}", event.getMessageGroupId(), e);
                recipientsToSave.add(Recipient.builder().messageGroup(messageGroup).status(ProcessingStatus.FAILED).errorCode("20000").errorMessage("encryption failed").build());
            }
        }

        saveRecipientBatch(recipientsToSave);
    }

    @Override
    public PageResponse<RecipientResponse> searchRecipients(String messageGroupId, Pageable pageable) {
        Page<Recipient> recipientPage = recipientRepository.findByMessageGroup_MessageGroupId(messageGroupId, pageable);

        List<RecipientResponse> content = recipientPage.getContent().stream()
                .map(RecipientResponse::from)
                .collect(Collectors.toList());

        return PageResponse.<RecipientResponse>builder()
                .content(content)
                .totalElements(recipientPage.getTotalElements())
                .totalPages(recipientPage.getTotalPages())
                .pageNumber(recipientPage.getNumber())
                .pageSize(recipientPage.getSize())
                .build();
    }
}