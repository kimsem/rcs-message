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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                    request.getMasterId(),
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
        // 마스터ID로 검색
        else if (request.getMasterId() != null) {
            page = historyRepository.findByMasterId(request.getMasterId(), pageable);
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
                request.getMasterId() != null &&
                request.getStatus() != null;
    }

    private boolean hasDateRangeOnly(MessageHistorySearchRequest request) {
        return request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getBrandId() == null &&
                request.getChatbotId() == null &&
                request.getMessageGroupId() == null &&
                request.getMasterId() == null &&
                request.getStatus() == null;
    }

   @Override
   @Transactional
   public void saveMessageHistory(List<MessageSendEvent> events) {
       List<MessageHistory> entities = events.stream()
               .map(this::convertToEntity)
               .collect(Collectors.toList());

       // 대량 데이터 일괄 저장
       historyRepository.saveAll(entities);
   }


   @Override
   @Transactional
   public void updateMessageStatus(MessageResultEvent event) {
       MessageHistory history = historyRepository.findById(event.getMessageId())
               .orElseThrow(() -> new RuntimeException("Message history not found: " + event.getMessageId()));
           
       history.updateStatus(MessageStatus.valueOf(event.getStatus()), event.getResultCode(), event.getResultMessage());
       historyRepository.save(history);
   }

   private MessageHistoryResponse convertToResponse(MessageHistory entity) {
       return MessageHistoryResponse.builder()
               .messageId(entity.getMessageId())
               .messageGroupId(entity.getMessageGroupId())
               .brandId(entity.getBrandId())
               .templateId(entity.getTemplateId())
               .chatbotId(entity.getChatbotId())
               .content(entity.getContent())
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
}

