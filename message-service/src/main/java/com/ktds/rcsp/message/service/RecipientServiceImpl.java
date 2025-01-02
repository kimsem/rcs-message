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
import java.time.LocalDateTime;


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
            MessageGroupSummaryRepository messageGroupSummaryRepository ) {  // 생성자 주입 추가
        this.eventPublisher = eventPublisher;
        this.encryptionService = encryptionService;
        this.messageGroupSummaryRepository = messageGroupSummaryRepository;
    }

    @Override
    @Async("recipientProcessorExecutor")
    public void processRecipientFile(String messageGroupId, MultipartFile file, String masterId,
                                     String brandId, String templateId, String chatbotId) {
        log.info("Start processing file for messageGroupId: {}", messageGroupId);

        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. MessageGroupSummary 생성 및 저장
            MessageGroupSummary messageGroup = MessageGroupSummary.builder()
                    .messageGroupId(messageGroupId)
                    .masterId(masterId)
                    .brandId(brandId)
                    .templateId(templateId)
                    .chatbotId(chatbotId)
                    .totalCount(0)
                    .processedCount(0)
                    .status("UPLOADING")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            messageGroupSummaryRepository.saveAndFlush(messageGroup);
            log.info("MessageGroupSummary saved successfully: {}", messageGroupId);

            // 기존 로직 유지
            initializeStatusMaps(messageGroupId);

            byte[] fileContent = file.getBytes();
            String filename = file.getOriginalFilename();

            if (filename == null) {
                throw new RuntimeException("파일명이 없습니다.");
            }

            try (InputStream is = new ByteArrayInputStream(fileContent)) {
                if (filename.endsWith(".csv")) {
                    processCsvFile(messageGroupId, is);
                } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                    processExcelFile(messageGroupId, is);
                } else {
                    throw new RuntimeException("지원하지 않는 파일 형식입니다.");
                }
            }

            // 2. 최종 상태 업데이트를 비동기 리스너에서 처리
            // EventHubMessageSubscriber에서 모든 데이터 처리 후 상태 변경
            log.info("File processing initiated for messageGroupId: {}", messageGroupId);

        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            updateMessageGroupStatus(messageGroupId, "FAILED");
            throw new RuntimeException("Failed to process file", e);
        }
    }

    // 추가해야 할 초기화 메서드
    private void initializeStatusMaps(String messageGroupId) {
        statusMap.put(messageGroupId, ProcessingStatus.UPLOADING);
        totalCountMap.put(messageGroupId, new AtomicInteger(0));
        processedCountMap.put(messageGroupId, new AtomicInteger(0));
        successCountMap.put(messageGroupId, new AtomicInteger(0));
        failCountMap.put(messageGroupId, new AtomicInteger(0));
    }

    private void updateMessageGroupStatus(String messageGroupId, String status) {
        try {
            MessageGroupSummary messageGroup = messageGroupSummaryRepository
                    .findById(messageGroupId)
                    .orElseThrow(() -> new RuntimeException("Message group not found"));
            messageGroup.setStatus(status);
            messageGroup.setUpdatedAt(LocalDateTime.now());
            messageGroupSummaryRepository.save(messageGroup);
        } catch (Exception e) {
            log.error("Error updating message group status: {}", e.getMessage(), e);
        }
    }

    @Override
    public UploadProgressResponse getUploadProgress(String messageGroupId) {
        try {
            MessageGroupSummary summary = messageGroupSummaryRepository
                    .findById(messageGroupId)
                    .orElseThrow(() -> new RuntimeException("Message group not found"));

            AtomicInteger success = successCountMap.get(messageGroupId);
            AtomicInteger fail = failCountMap.get(messageGroupId);

            return UploadProgressResponse.builder()
                    .totalCount(summary.getTotalCount())
                    .processedCount(summary.getProcessedCount())
                    .successCount(success != null ? success.get() : summary.getProcessedCount())
                    .failCount(fail != null ? fail.get() : 0)
                    .status(summary.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("Error getting upload progress", e);
            return UploadProgressResponse.builder()
                    .status("FAILED")
                    .build();
        }
    }


//    @Override
//    public UploadProgressResponse getUploadProgress(String messageGroupId) {
//        ProcessingStatus status = statusMap.get(messageGroupId);
//        AtomicInteger total = totalCountMap.get(messageGroupId);
//        AtomicInteger processed = processedCountMap.get(messageGroupId);
//        AtomicInteger success = successCountMap.get(messageGroupId);
//        AtomicInteger fail = failCountMap.get(messageGroupId);
//
//        if (status == null || total == null || processed == null ||
//                success == null || fail == null) {
//            // 처리 중이 아닌 경우 DB에서 조회
//            MessageGroupSummary summary = messageGroupSummaryRepository
//                    .findById(messageGroupId)
//                    .orElseThrow(() -> new RuntimeException("Message group not found"));
//
//            return UploadProgressResponse.builder()
//                    .totalCount(summary.getTotalCount())
//                    .processedCount(summary.getProcessedCount())
//                    .successCount(0)  // DB에 없는 정보는 0으로
//                    .failCount(0)
//                    .status(summary.getStatus())
//                    .build();
//        }
//
//        return UploadProgressResponse.builder()
//                .totalCount(total.get())
//                .processedCount(processed.get())
//                .successCount(success.get())
//                .failCount(fail.get())
//                .status(status.name())
//                .build();
//    }


    private void processCsvFile(String messageGroupId, InputStream is) throws IOException {
        log.debug("Processing CSV file for messageGroupId: {}", messageGroupId);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (var record : csvParser) {
                try {
                    String phoneNumber = record.get("phoneNumber");
                    if (isValidPhoneNumber(phoneNumber)) {
                        RecipientUploadEvent event = RecipientUploadEvent.builder()
                                .messageGroupId(messageGroupId)
                                .phoneNumber(phoneNumber.trim())
                                .status("PROCESSING")
                                .build();

                        eventPublisher.publishUploadEvent(event);
                        log.debug("Published event for phone: {}", phoneNumber);
                    }
                } catch (Exception e) {
                    log.error("Error processing record: {}", e.getMessage(), e);
                }
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.trim().isEmpty();
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

            int actualRowCount = sheet.getPhysicalNumberOfRows() - 1;
            AtomicInteger totalCount = totalCountMap.get(messageGroupId);
            if (totalCount == null) {
                log.error("상태 맵이 초기화되지 않았습니다. messageGroupId: {}", messageGroupId);
                throw new RuntimeException("상태 맵이 초기화되지 않았습니다.");
            }

            totalCount.set(actualRowCount);

            MessageGroupSummary messageGroup = messageGroupSummaryRepository.findById(messageGroupId)
                    .orElseThrow(() -> new RuntimeException("Message group not found"));
            messageGroup.setTotalCount(actualRowCount);
            messageGroupSummaryRepository.save(messageGroup);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String phoneNumber = getCellValue(row.getCell(phoneNumberColIdx));
                    String name = getCellValue(row.getCell(nameColIdx));
                    if (isValidData(phoneNumber, name)) {
                        // 여기서 이벤트를 직접 발행하고, publishToEventHub() 메소드는 호출하지 않음
                        RecipientUploadEvent event = RecipientUploadEvent.builder()
                                .messageGroupId(messageGroupId)
                                .phoneNumber(phoneNumber)
                                .name(name)
                                .status(ProcessingStatus.UPLOADING.name())
                                .build();
                        eventPublisher.publishUploadEvent(event);

                        // 카운터 업데이트
                        AtomicInteger processedCount = processedCountMap.get(messageGroupId);
                        AtomicInteger successCount = successCountMap.get(messageGroupId);
                        if (processedCount != null && successCount != null) {
                            processedCount.incrementAndGet();
                            successCount.incrementAndGet();
                        }
                    }
                }
            }
        }
    }


    private void publishToEventHub(String messageGroupId, String phoneNumber, String name) {
        AtomicInteger processedCount = processedCountMap.get(messageGroupId);
        AtomicInteger successCount = successCountMap.get(messageGroupId);
        AtomicInteger failCount = failCountMap.get(messageGroupId);

        if (processedCount == null || successCount == null || failCount == null) {
            log.error("상태 맵이 초기화되지 않았습니다. messageGroupId: {}", messageGroupId);
            throw new RuntimeException("상태 맵이 초기화되지 않았습니다.");
        }

        try {
            RecipientUploadEvent event = RecipientUploadEvent.builder()
                    .messageGroupId(messageGroupId)
                    .phoneNumber(phoneNumber)
                    .name(name)
                    .status(ProcessingStatus.UPLOADING.name())
                    .build();

            eventPublisher.publishUploadEvent(event);

            processedCount.incrementAndGet();
            successCount.incrementAndGet();

            log.debug("Published data to EventHub - MessageGroupId: {}", messageGroupId);
        } catch (Exception e) {
            failCount.incrementAndGet();
            log.error("Failed to encrypt and publish to EventHub - MessageGroupId: {}, error: {}",
                    messageGroupId, e.getMessage(), e);
            throw new RuntimeException("EventHub 발행 실패", e);
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