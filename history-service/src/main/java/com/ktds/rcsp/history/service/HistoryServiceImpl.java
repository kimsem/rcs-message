package com.ktds.rcsp.history.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.history.domain.MessageHistory;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.repository.HistoryRepository;
import com.ktds.rcsp.history.repository.MessageHistorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

   private final HistoryRepository historyRepository;

   @Override
   @Transactional(readOnly = true)
   public PageResponse<MessageHistoryResponse> searchMessages(MessageHistorySearchRequest request) {
       Page<MessageHistory> page = historyRepository.findAll(
           MessageHistorySpecification.searchMessageHistory(request),
           PageRequest.of(request.getPage(), request.getSize())
       );

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

   @Override
   @Transactional
   @CacheEvict(value = "messageHistory", key = "#history.messageId")
   public MessageHistoryResponse saveMessageHistory(MessageHistoryResponse history) {
       MessageHistory entity = convertToEntity(history);
       MessageHistory savedEntity = historyRepository.save(entity);
       return convertToResponse(savedEntity);
   }

   @Override
   @Transactional
   @CacheEvict(value = "messageHistory", key = "#messageId")
   public void updateMessageStatus(String messageId, String status, String resultCode, String resultMessage) {
       MessageHistory history = historyRepository.findById(messageId)
               .orElseThrow(() -> new RuntimeException("Message history not found: " + messageId));
           
       history.updateStatus(MessageStatus.valueOf(status), resultCode, resultMessage);
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

   private MessageHistory convertToEntity(MessageHistoryResponse dto) {
       return MessageHistory.builder()
               .messageId(dto.getMessageId())
               .messageGroupId(dto.getMessageGroupId())
               .brandId(dto.getBrandId())
               .templateId(dto.getTemplateId())
               .chatbotId(dto.getChatbotId())
               .content(dto.getContent())
               .status(dto.getStatus())
               .resultCode(dto.getResultCode())
               .resultMessage(dto.getResultMessage())
               .createdAt(dto.getCreatedAt())
               .updatedAt(dto.getUpdatedAt())
               .build();
   }
}

