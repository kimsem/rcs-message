package com.ktds.rcsp.history.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.domain.MessageHistory;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.repository.HistoryRepository;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final Timer historySearchTimer;
    private final Counter historySearchTotalCounter;
    private final Counter historySearchErrorCounter;
    private final Timer dbQueryTimer;
    private final Counter dbConnectionCounter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageHistoryResponse> searchMessages(MessageHistorySearchRequest request) {
        Timer.Sample searchSample = Timer.start();
        historySearchTotalCounter.increment();

        try {
            Timer.Sample dbSample = Timer.start();
            dbConnectionCounter.increment();

            Page<MessageHistory> page = historyRepository.findBySearchCriteria(
                    request.getMasterId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getBrandId(),
                    request.getChatbotId(),
                    request.getMessageGroupId(),
                    request.getStatus(),  // .name() 제거
                    PageRequest.of(request.getPage(), request.getSize())
            );

            dbSample.stop(dbQueryTimer);

            PageResponse<MessageHistoryResponse> response = PageResponse.<MessageHistoryResponse>builder()
                    .content(page.getContent().stream()
                            .map(this::convertToResponse)
                            .toList())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .build();

            searchSample.stop(historySearchTimer);
            return response;

        } catch (Exception e) {
            historySearchErrorCounter.increment();
            log.error("Error searching message history", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void saveMessageHistory(List<MessageSendEvent> events) {
        Timer.Sample dbSample = Timer.start();
        dbConnectionCounter.increment();

        try {
            List<MessageHistory> entities = events.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            historyRepository.saveAll(entities);
            dbSample.stop(dbQueryTimer);

        } catch (Exception e) {
            historySearchErrorCounter.increment();
            log.error("Error saving message history", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void updateMessageStatus(MessageResultEvent event) {
        Timer.Sample dbSample = Timer.start();
        dbConnectionCounter.increment();

        try {
            MessageHistory history = historyRepository.findById(event.getMessageId())
                    .orElseThrow(() -> new RuntimeException("Message history not found: " + event.getMessageId()));

            history.updateStatus(MessageStatus.valueOf(event.getStatus()),
                    event.getResultCode(),
                    event.getResultMessage());

            historyRepository.save(history);
            dbSample.stop(dbQueryTimer);

        } catch (Exception e) {
            historySearchErrorCounter.increment();
            log.error("Error updating message status", e);
            throw e;
        }
    }

    private void validateDateRange(MessageHistorySearchRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }
            if (ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) > 365) {
                throw new IllegalArgumentException("Date range cannot exceed 1 year");
            }
        }
    }

    private MessageHistoryResponse convertToResponse(MessageHistory entity) {
        return MessageHistoryResponse.builder()
                .messageId(entity.getMessageId())
                .messageGroupId(entity.getMessageGroupId())
                .brandId(entity.getBrandId())
                .templateId(entity.getTemplateId())
                .chatbotId(entity.getChatbotId())
                .content(entity.getContent())
                .phoneNumber(entity.getEncryptedPhone())
                .masterId(entity.getMasterId())
                .status(entity.getStatus())
                .resultCode(entity.getResultCode())
                .resultMessage(entity.getResultMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private MessageHistory convertToEntity(MessageSendEvent event) {
        return MessageHistory.builder()
                .messageId(event.getMessageId())
                .messageGroupId(event.getMessageGroupId())
                .masterId(event.getMasterId())
                .brandId(event.getBrandId())
                .templateId(event.getTemplateId())
                .chatbotId(event.getChatbotId())
                .content(event.getContent())
                .encryptedPhone(event.getRecipientPhone())
                .status(MessageStatus.valueOf(event.getStatus()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void validateMandatoryParameters(MessageHistorySearchRequest request) {
        if (request.getMasterId() == null || request.getMasterId().isBlank()) {
            throw new IllegalArgumentException("MasterId is mandatory");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is mandatory");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("End date is mandatory");
        }
    }
}