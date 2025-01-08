package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.dto.ErrorCode;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.exception.BusinessException;
import com.ktds.rcsp.message.domain.*;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.repository.MessageGroupRepository;
import com.ktds.rcsp.message.repository.MessageRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

   private final MessageRepository messageRepository;
   private final RecipientRepository recipientRepository;
   private final MessageGroupRepository messageGroupRepository;
   private final RecipientService recipientService;
   private final EncryptionService encryptionService;
   private final MessageProcessor messageProcessor;

   @Override
   public MessageSendResponse sendMessage(MessageSendRequest request) {
       // messageGroup update
       MessageGroup messageGroup = MessageGroup.builder()
               .messageGroupId(request.getMessageGroupId())
               .masterId(request.getMasterId())
               .brandId(request.getBrandId())
               .templateId(request.getTemplateId())
               .chatbotId(request.getChatbotId())
               .status(MessageGroupStatus.READY)
               .build();

       messageGroupRepository.save(messageGroup);

       List<Recipient> recipients = recipientRepository.findByMessageGroup_MessageGroupId(request.getMessageGroupId());

       if (recipients.isEmpty()) {
           throw new BusinessException(ErrorCode.NO_RECIPIENTS);
       }

       messageProcessor.processMessagesAsync(request, recipients, messageGroup);

       return MessageSendResponse.builder()
               .messageGroupId(request.getMessageGroupId())
               .status("SUCCESS")
               .build();
   }



   @Override
   @Transactional
   public void uploadRecipients(String messageGroupId, String masterId, MultipartFile file) {
       try {
           int totalCount = recipientService.getTotalCount(file);

           // 1. MessageGroup을 먼저 저장
           if(messageGroupRepository.existsByMessageGroupId(messageGroupId)) throw new BusinessException(ErrorCode.DUPLICATE_MESSAGE_GROUP_ID);
           MessageGroup messageGroup = MessageGroup.builder()
                   .messageGroupId(messageGroupId)
                   .masterId(masterId)
                   .status(MessageGroupStatus.UPLOADING)
                   .totalCount(totalCount)
                   .build();
           messageGroupRepository.save(messageGroup);

           recipientService.processRecipientFile(messageGroupId, file);
       } catch (Exception e) {
           log.error("Failed to upload recipients", e);
           throw new RuntimeException("Failed to upload recipients", e);
       }
   }

   @Override
   @Transactional
   public void processMessageResult(MessageResultEvent event) {
       Message message = messageRepository.findById(event.getMessageId())
               .orElseThrow(() -> new RuntimeException("Message not found"));
       messageRepository.delete(message); // EventHub에서 결과 적재 받으면 발송DB에서 삭제
       if(message.getMessageGroup().getStatus() != MessageGroupStatus.COMPLETED) {
           MessageGroup messageGroup = message.getMessageGroup();
           messageGroup.updateStatus();
           messageGroupRepository.save(messageGroup);
       }
   }
   

   @Override
   @Transactional(readOnly = true)
   public UploadProgressResponse getUploadProgress(String messageGroupId) {
       Integer totalCount = getTotalCountByMessageGroupId(messageGroupId);

       List<Object[]> statusCountList = recipientRepository.countByMessageGroup_MessageGroupIdAndStatusGroup(messageGroupId);
       Map<ProcessingStatus, Long> statusCountMap = statusCountList.stream()
               .collect(Collectors.toMap(
                       row -> (ProcessingStatus) row[0], // 첫 번째 값은 상태 (ProcessingStatus)
                       row -> (Long) row[1] // 두 번째 값은 카운트
               ));
       // 각 상태별 count 가져오기
       long successCount = statusCountMap.getOrDefault(ProcessingStatus.COMPLETED, 0L);
       long failCount = statusCountMap.getOrDefault(ProcessingStatus.FAILED, 0L);

       return UploadProgressResponse.builder()
               .processedCount((int) (successCount + failCount))
               .successCount((int) successCount)
               .failCount((int) failCount)
               .totalCount(totalCount)
               .status(totalCount == (successCount + failCount) ? "COMPLETED" : "PROCESSING")
               .build();
   }

    private Integer getTotalCountByMessageGroupId(String messageGroupId) {
        return messageGroupRepository.getTotalCountByMessageGroupId(messageGroupId);
    }

    @Override
    public void processMessageResultEvent(List<MessageSendEvent> events) {
        List<Message> messages = new ArrayList<>();
        for (MessageSendEvent event : events) {
            try {
                // 캐시를 통해 MessageGroup 조회
                MessageGroup messageGroup = messageProcessor.getMessageGroup(event.getMessageGroupId());

                Message message = Message.builder()
                        .messageId(event.getMessageId())
                        .messageGroup(messageGroup)
                        .recipientId(encryptionService.encrypt(event.getRecipientPhone()))
                        .content(event.getContent())
                        .status(MessageStatus.PENDING)
                        .build();

                messages.add(message);
            } catch (Exception e) {
                log.error("Failed to process message send event - messageId: {}", event.getMessageId(), e);
            }
        }

        try {
            messageRepository.saveAll(messages);
            log.info("Batch of {} messages saved successfully.", messages.size());
        } catch (Exception e) {
            log.error("Failed to save batch of messages", e);
        }
    }

}
