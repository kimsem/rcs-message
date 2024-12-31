package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
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

            int totalCount = 0;
            for (CSVRecord record : csvParser) {
                String phoneNumber = record.get("phoneNumber");
                encryptAndSaveRecipient(messageGroupId, phoneNumber);
                totalCount++;
            }

            eventPublisher.publishUploadEvent(RecipientUploadEvent.builder()
                    .messageGroupId(messageGroupId)
                    .fileName(file.getOriginalFilename())
                    .totalCount(totalCount)
                    .build());

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

            Recipient recipient = Recipient.builder()
                    .messageGroupId(messageGroupId)
                    .phoneNumber(phoneNumber)
                    .encryptedPhoneNumber(encryptedPhoneNumber)
                    .status(ProcessingStatus.COMPLETED)
                    .build();

            recipientRepository.save(recipient);
        } catch (Exception e) {
            log.error("Failed to encrypt and save recipient", e);
            throw new RuntimeException("Failed to encrypt and save recipient", e);
        }
    }
}