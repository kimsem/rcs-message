package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.MessageGroupSummary;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageGroupSummaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Async;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
public class RecipientServiceImpl implements RecipientService {

    private final EventHubMessagePublisher eventPublisher;
    private final EncryptionService encryptionService;
    private final MessageGroupSummaryRepository messageGroupSummaryRepository;

    // 실시간 처리 상태를 위한 Thread-safe 컬렉션
    private final Map<String, ProcessingStatus> statusMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> totalCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> processedCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> successCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failCountMap = new ConcurrentHashMap<>();

    @Autowired
    public RecipientServiceImpl(
            EventHubMessagePublisher eventPublisher,
            EncryptionService encryptionService,
            MessageGroupSummaryRepository messageGroupSummaryRepository) {  // 생성자 주입 추가
        this.eventPublisher = eventPublisher;
        this.encryptionService = encryptionService;
        this.messageGroupSummaryRepository = messageGroupSummaryRepository;
    }

    @Override
    @Async("recipientProcessorExecutor")
    public void processRecipientFile(String messageGroupId, MultipartFile file, String masterId,
                                     String brandId, String templateId, String chatbotId) {
        try {
            // 파일 데이터를 먼저 메모리에 복사
            byte[] fileBytes = file.getBytes();

            log.info("Start processing file for messageGroupId: {}", messageGroupId);

            // MessageGroupSummary 저장 시도
            try {
                MessageGroupSummary messageGroup = MessageGroupSummary.builder()
                        .messageGroupId(messageGroupId)
                        .masterId(masterId)
                        .brandId(brandId)
                        .templateId(templateId)
                        .chatbotId(chatbotId)
                        .totalCount(0)
                        .processedCount(0)
                        .status("UPLOADING")
                        .build();

                messageGroupSummaryRepository.saveAndFlush(messageGroup);
                log.info("MessageGroupSummary saved successfully: {}", messageGroupId);
            } catch (Exception e) {
                log.error("Failed to save MessageGroupSummary: {}", messageGroupId, e);
                statusMap.put(messageGroupId, ProcessingStatus.FAILED);
                throw e;
            }


            // 초기 상태 설정
            statusMap.put(messageGroupId, ProcessingStatus.UPLOADING);
            totalCountMap.put(messageGroupId, new AtomicInteger(0));
            processedCountMap.put(messageGroupId, new AtomicInteger(0));
            successCountMap.put(messageGroupId, new AtomicInteger(0));
            failCountMap.put(messageGroupId, new AtomicInteger(0));

            // 메모리의 byte[] 데이터로 처리
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                String filename = file.getOriginalFilename();
                if (filename == null) {
                    throw new RuntimeException("파일명이 없습니다.");
                }

                if (filename.endsWith(".csv")) {
                    processCsvFile(messageGroupId, is);
                } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                    processExcelFile(messageGroupId, is);
                }
            }


            statusMap.put(messageGroupId, ProcessingStatus.COMPLETED);
            log.info("File processing completed for messageGroupId: {}", messageGroupId);

            // 일정 시간 후 데이터 정리 (예: 30분 후)
            new Thread(() -> {
                try {
                    Thread.sleep(1800000); // 30분
                    cleanupProcessingData(messageGroupId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (
                Exception e) {
            updateMessageGroupStatus(messageGroupId, "FAILED");
            log.error("Failed to process file", e);
            throw new RuntimeException("Failed to process file", e);
        }
    }

    private void updateMessageGroupStatus(String messageGroupId, String status) {
        MessageGroupSummary messageGroup = messageGroupSummaryRepository.findById(messageGroupId)
                .orElseThrow(() -> new RuntimeException("Message group not found"));
        messageGroup.setStatus(status);
        messageGroupSummaryRepository.save(messageGroup);
    }

    @Override
    public UploadProgressResponse getUploadProgress(String messageGroupId) {
        ProcessingStatus status = statusMap.getOrDefault(messageGroupId, ProcessingStatus.UPLOADING);
        int total = totalCountMap.getOrDefault(messageGroupId, new AtomicInteger(0)).get();
        int processed = processedCountMap.getOrDefault(messageGroupId, new AtomicInteger(0)).get();
        int success = successCountMap.getOrDefault(messageGroupId, new AtomicInteger(0)).get();
        int fail = failCountMap.getOrDefault(messageGroupId, new AtomicInteger(0)).get();

        return UploadProgressResponse.builder()
                .totalCount(total)
                .processedCount(processed)
                .successCount(success)
                .failCount(fail)
                .status(status.name())
                .build();
    }


    private void processCsvFile(String messageGroupId, InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withHeader()
                    .withIgnoreHeaderCase(true)
                    .withTrim(true)
                    .parse(reader);

            // 전체 레코드 수 설정
            totalCountMap.get(messageGroupId).set((int) csvParser.stream().count());

            for (var record : csvParser) {
                String phoneNumber = record.get("phoneNumber");
                String name = record.get("name");
                if (isValidData(phoneNumber, name)) {
                    publishToEventHub(messageGroupId, phoneNumber.trim(), name.trim());
                }
            }
        }
    }

    private void processExcelFile(String messageGroupId, InputStream is) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            int phoneNumberColIdx = findPhoneNumberColumn(headerRow);
            int nameColIdx = findNameColumn(headerRow);

            if (phoneNumberColIdx == -1 || nameColIdx == -1) {
                throw new RuntimeException("필수 컬럼을 찾을 수 없습니다.");
            }

            // 전체 레코드 수 설정
            totalCountMap.get(messageGroupId).set(sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String phoneNumber = getCellValue(row.getCell(phoneNumberColIdx));
                    String name = getCellValue(row.getCell(nameColIdx));
                    if (isValidData(phoneNumber, name)) {
                        publishToEventHub(messageGroupId, phoneNumber.trim(), name.trim());
                    }
                }
            }
        }
    }

    private void publishToEventHub(String messageGroupId, String phoneNumber, String name) {
        try {
            // 전화번호와 이름을 암호화
//            String encryptedPhoneNumber = encryptionService.encrypt(phoneNumber);
//            String encryptedName = encryptionService.encrypt(name);

            RecipientUploadEvent event = RecipientUploadEvent.builder()
                    .messageGroupId(messageGroupId)
                    .phoneNumber(phoneNumber)
                    .name(name)
                    .status(ProcessingStatus.UPLOADING.name())
                    .build();

            eventPublisher.publishUploadEvent(event);

            // 처리 카운트 증가
            processedCountMap.get(messageGroupId).incrementAndGet();
            successCountMap.get(messageGroupId).incrementAndGet();

            log.debug("Published data to EventHub - MessageGroupId: {}", messageGroupId);
        } catch (Exception e) {
            failCountMap.get(messageGroupId).incrementAndGet();
            log.error("Failed to encrypt and publish to EventHub - MessageGroupId: {}", messageGroupId, e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.format("%.0f", cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private int findPhoneNumberColumn(Row headerRow) {
        return findColumnByHeader(headerRow, new String[]{"phone", "전화", "휴대폰", "mobile", "phonenumber"});
    }

    private int findNameColumn(Row headerRow) {
        return findColumnByHeader(headerRow, new String[]{"name", "이름"});
    }

    private int findColumnByHeader(Row headerRow, String[] possibleHeaders) {
        for (Cell cell : headerRow) {
            String value = cell.getStringCellValue().toLowerCase().trim();
            for (String header : possibleHeaders) {
                if (value.contains(header.toLowerCase())) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1;
    }

    private boolean isValidData(String phoneNumber, String name) {
        return phoneNumber != null && !phoneNumber.trim().isEmpty()
                && name != null && !name.trim().isEmpty();
    }

//    @Override
//    public int getTotalCount(MultipartFile file) {
//        try {
//            try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
//                String filename = file.getOriginalFilename();
//                if (filename == null) {
//                    throw new RuntimeException("파일명이 없습니다.");
//                }
//
//                if (filename.endsWith(".csv")) {
//                    return getCsvTotalCount(is);
//                } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
//                    return getExcelTotalCount(is);
//                } else {
//                    throw new RuntimeException("지원하지 않는 파일 형식입니다.");
//                }
//            }
//        } catch (IOException e) {
//            log.error("Failed to get total count from file", e);
//            throw new RuntimeException("Failed to get total count from file", e);
//        }
//    }

    private int getCsvTotalCount(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            return (int) csvParser.stream().count();
        }
    }

    private int getExcelTotalCount(InputStream is) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            return sheet.getPhysicalNumberOfRows() - 1; // Exclude header row
        }
    }

    private void cleanupProcessingData(String messageGroupId) {
        statusMap.remove(messageGroupId);
        totalCountMap.remove(messageGroupId);
        processedCountMap.remove(messageGroupId);
        successCountMap.remove(messageGroupId);
        failCountMap.remove(messageGroupId);
    }

}