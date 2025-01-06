package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.exception.BusinessException;
import com.ktds.rcsp.message.domain.*;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageGroupRepository;
import com.ktds.rcsp.message.repository.MessageRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

   private final MessageRepository messageRepository;
   private final RecipientRepository recipientRepository;
   private final MessageGroupRepository messageGroupRepository;
   private final EventHubMessagePublisher eventPublisher;
   private final RecipientService recipientService;
   private final EncryptionService encryptionService;

   @Override
   @Transactional
   public MessageSendResponse sendMessage(MessageSendRequest request) {
       // messageGroup update
       MessageGroup messageGroup = MessageGroup.builder()
               .messageGroupId(request.getMessageGroupId())
               .brandId(request.getBrandId())
               .templateId(request.getTemplateId())
               .chatbotId(request.getChatbotId())
               .status(MessageGroupStatus.READY)
               .build();

       messageGroupRepository.save(messageGroup);

       List<Recipient> recipients = recipientRepository.findByMessageGroup_MessageGroupId(request.getMessageGroupId());

       if (recipients.isEmpty()) {
           throw new BusinessException("메시지 그룹에 수신자가 없습니다.");
       }

       // 2. 각 수신자별로 메시지 생성 및 처리
       List<Message> messages = recipients.stream()
               .map(recipient -> Message.builder()
                       .messageId(UUID.randomUUID().toString())
                       .messageGroup(messageGroup)
                       .recipientId(recipient.getEncryptedPhone())
                       .content(request.getContent())
                       .status(MessageStatus.PENDING)
                       .build())
               .toList();


       // 3. 메시지들을 Event Hub에 발송 요청 적재
       messages.forEach(message -> {
           MessageSendEvent sendEvent = MessageSendEvent.builder()
                   .messageId(UUID.randomUUID().toString())
                   .messageGroupId(messageGroup.getMessageGroupId())
                   .brandId(message.getMessageGroup().getBrandId())
                   .templateId(message.getMessageGroup().getTemplateId())
                   .chatbotId(message.getMessageGroup().getChatbotId())
                   .content(message.getContent())
                   .recipientPhone(encryptionService.decrypt(message.getRecipientId()))
                   .status(MessageStatus.PENDING.name())
                   .build();
           eventPublisher.publishSendEvent(sendEvent);
       });

       return MessageSendResponse.builder()
               .messageGroupId(request.getMessageGroupId())
               .status("SUCCESS")
               .build();
   }

   @Override
   @Transactional
   public void uploadRecipients(String messageGroupId, MultipartFile file) {
       try {
           // 1. MessageGroup을 먼저 저장
           MessageGroup messageGroup = MessageGroup.builder()
                   .messageGroupId(messageGroupId)
                   .status(MessageGroupStatus.READY)
                   .build();
           if(messageGroupRepository.existsByMessageGroupId(messageGroupId)) throw new BusinessException("이미 존재하는 메시지 그룹 아이디입니다.");
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
   }

   @Override
   @Transactional(readOnly = true)
   public UploadProgressResponse getUploadProgress(String messageGroupId) {

       List<Object[]> statusCountList = recipientRepository.countByMessageGroup_MessageGroupIdAndStatusGroup(messageGroupId);
       Map<ProcessingStatus, Long> statusCountMap = statusCountList.stream()
               .collect(Collectors.toMap(
                       row -> (ProcessingStatus) row[0], // 첫 번째 값은 상태 (ProcessingStatus)
                       row -> (Long) row[1] // 두 번째 값은 카운트
               ));
       // 각 상태별 count 가져오기
       long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
       long successCount = statusCountMap.getOrDefault(ProcessingStatus.COMPLETED, 0L);
       long failCount = statusCountMap.getOrDefault(ProcessingStatus.FAILED, 0L);

       return UploadProgressResponse.builder()
               .processedCount((int) (successCount + failCount))
               .successCount((int) successCount)
               .failCount((int) failCount)
               .totalCount((int) totalCount)
               .status(totalCount == (successCount + failCount) ? "COMPLETED" : "PROCESSING")
               .build();
   }

    @Override
    public void processMessageResultEvent(MessageSendEvent event) {
        try {
            MessageGroup messageGroup = messageGroupRepository.findById(event.getMessageGroupId())
                    .orElseThrow(() -> new BusinessException("Message group not found: " + event.getMessageGroupId()));

            // 1. Message 엔티티 생성
            Message message = Message.builder()
                    .messageId(event.getMessageId())
                    .messageGroup(messageGroup)
                    .recipientId(encryptionService.encrypt(event.getRecipientPhone()))
                    .content(event.getContent())
                    .status(MessageStatus.PENDING)  // 초기 상태
                    .build();

            // 2. DB 저장
            message = messageRepository.save(message);
            log.info("Message saved - messageId: {}, status: {}",
                    message.getMessageId(), message.getStatus());

        } catch (Exception e) {
            log.error("Failed to process message send event - messageId: {}",
                    event.getMessageId(), e);

            // 4. 실패 시 에러 이벤트 발행
//            publishFailureEvent(event.getMessageId(), e.getMessage());
            throw new BusinessException("Failed to process message", e);
        }
    }
}
