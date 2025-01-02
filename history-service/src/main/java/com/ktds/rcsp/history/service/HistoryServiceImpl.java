package com.ktds.rcsp.history.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.domain.MessageHistory;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

   private final HistoryRepository historyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageHistoryResponse> searchMessages(MessageHistorySearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<MessageHistory> page;

        // 검색 조건이 모두 있는 경우
        if (hasAllSearchCriteria(request)) {
            page = historyRepository.findBySearchCriteria(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getBrandId(),
                    request.getChatbotId(),
                    request.getMessageGroupId(),
                    request.getStatus(),
                    pageable
            );
        }
        // 날짜 범위만 있는 경우
        else if (hasDateRangeOnly(request)) {
            page = historyRepository.findByCreatedAtBetween(
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable
            );
        }
        // 브랜드ID로 검색
        else if (request.getBrandId() != null) {
            page = historyRepository.findByBrandId(request.getBrandId(), pageable);
        }
        // 발신번호ID로 검색
        else if (request.getChatbotId() != null) {
            page = historyRepository.findByChatbotId(request.getChatbotId(), pageable);
        }
        // 메시지그룹ID로 검색
        else if (request.getMessageGroupId() != null) {
            page = historyRepository.findByMessageGroupId(request.getMessageGroupId(), pageable);
        }
        // 상태로 검색
        else if (request.getStatus() != null) {
            page = historyRepository.findByStatus(request.getStatus(), pageable);
        }
        // 검색 조건이 없는 경우
        else {
            page = historyRepository.findAll(pageable);
        }

        return PageResponse.<MessageHistoryResponse>builder()
                .content(page.getContent().stream()
                        .map(this::convertToResponse)
                        .toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    // 검색 조건 체크 헬퍼 메소드들
    private boolean hasAllSearchCriteria(MessageHistorySearchRequest request) {
        return request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getBrandId() != null &&
                request.getChatbotId() != null &&
                request.getMessageGroupId() != null &&
                request.getStatus() != null;
    }

    private boolean hasDateRangeOnly(MessageHistorySearchRequest request) {
        return request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getBrandId() == null &&
                request.getChatbotId() == null &&
                request.getMessageGroupId() == null &&
                request.getStatus() == null;
    }

   @Override
   @Transactional
   public void saveMessageHistory(MessageSendEvent event) {
       MessageHistory entity = convertToEntity(event);
       historyRepository.save(entity);
   }


   @Override
   @Transactional
   public void updateMessageStatus(MessageResultEvent event) {
       MessageHistory history = historyRepository.findById(event.getMessageId())
               .orElseThrow(() -> new RuntimeException("Message history not found: " + event.getMessageId()));
           
       history.updateStatus(MessageStatus.valueOf(event.getStatus()), event.getResultCode(), event.getResultMessage());
       historyRepository.save(history);
   }

   @Cacheable(value = "messageHistory", key = "#messageId")
   public MessageHistoryResponse getMessageHistory(String messageId) {
       return historyRepository.findById(messageId)
               .map(this::convertToResponse)
               .orElseThrow(() -> new RuntimeException("Message history not found: " + messageId));
   }

   private MessageHistoryResponse convertToResponse(MessageHistory entity) {
       return MessageHistoryResponse.builder()
               .messageId(entity.getMessageId())
               .messageGroupId(entity.getMessageGroupId())
               .brandId(entity.getBrandId())
               .templateId(entity.getTemplateId())
               .chatbotId(entity.getChatbotId())
               .content(entity.getContent())
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
               .brandId(event.getBrandId())
               .templateId(event.getTemplateId())
               .chatbotId(event.getChatbotId())
               .content(event.getContent())
               .encryptedPhone(event.getRecipientPhone())
               .status(MessageStatus.valueOf(event.getStatus()))
               .createdAt(LocalDateTime.now())
               .build();
   }
}

