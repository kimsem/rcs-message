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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    private final PlatformTransactionManager transactionManager;
    private static final int BATCH_SIZE = 5000;
    private final JdbcTemplate jdbcTemplate;

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
    public void processBatches(List<List<Recipient>> allBatches) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 각 배치마다 비동기적으로 처리하도록 요청
        for (List<Recipient> batch : allBatches) {
            futures.add(saveBatchAsync(batch));
        }

        // 모든 비동기 작업이 끝날 때까지 기다리기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
    public void saveRecipientBatch(List<Recipient> recipients) {
        List<List<Recipient>> batches = createBatches(recipients, 1000); // 1000건씩 배치 나누기
        processBatches(batches); // 각 배치 비동기 처리
    }

    private List<List<Recipient>> createBatches(List<Recipient> recipients, int batchSize) {
        List<List<Recipient>> batches = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i += batchSize) {
            int end = Math.min(i + batchSize, recipients.size());
            batches.add(recipients.subList(i, end));
        }
        return batches;
    }

    @Async("executorService") // taskExecutor 사용
    public CompletableFuture<Void> saveBatchAsync(List<Recipient> batch) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 트랜잭션 관리
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);

                try {
                    // 배치 저장
                    recipientRepository.saveAll(batch);
                    entityManager.flush();
                    entityManager.clear();
                    transactionManager.commit(status); // 트랜잭션 커밋
                } catch (Exception e) {
                    transactionManager.rollback(status); // 예외 발생 시 롤백
                    log.error("Failed to save recipient batch", e);
                }
            } catch (Exception e) {
                log.error("Error processing batch", e);
            }
        }, executorService); // executorService 사용
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

//        saveRecipientBatch(recipientsToSave);
        saveRecipientBatchWithJdbcBatch(recipientsToSave);
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

    public void saveRecipientBatchWithJdbcBatch(List<Recipient> recipients) {
        String sql = "INSERT INTO recipients (message_group_id, encrypted_phone, status, error_code, error_message, created_at) VALUES (?, ?, ?, ?, ?,?)";

        // 배치 업데이트를 위한 파라미터 리스트 준비
        List<Object[]> batchArgs = recipients.stream()
                .map(recipient -> new Object[]{
                        recipient.getMessageGroup().getMessageGroupId(),
                        recipient.getEncryptedPhone(),
                        recipient.getStatus().name(),
                        recipient.getErrorCode(),
                        recipient.getErrorMessage(),
                        LocalDateTime.now()
                })
                .collect(Collectors.toList());

        // 배치 업데이트 실행
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}