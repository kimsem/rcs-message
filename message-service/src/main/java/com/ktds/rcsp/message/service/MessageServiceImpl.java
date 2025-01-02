package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.exception.BusinessException;
import com.ktds.rcsp.message.domain.Message;
import com.ktds.rcsp.message.domain.MessageStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

   private final MessageRepository messageRepository;
   private final RecipientRepository recipientRepository;
   private final EventHubMessagePublisher eventPublisher;
   private final RecipientService recipientService;
   private final EncryptionService encryptionService;

   @Override
   @Transactional
   public MessageSendResponse sendMessage(MessageSendRequest request) {

       List<Recipient> recipients = recipientRepository.findByMessageGroupId(request.getMessageGroupId());

       if (recipients.isEmpty()) {
           throw new BusinessException("메시지 그룹에 수신자가 없습니다.");
       }

       // 2. 각 수신자별로 메시지 생성 및 처리
       List<Message> messages = recipients.stream()
               .map(recipient -> Message.builder()
                       .messageId(UUID.randomUUID().toString())
                       .messageGroupId(request.getMessageGroupId())
                       .recipientId(String.valueOf(recipient.getId()))
                       .content(request.getContent())
                       .status(MessageStatus.PENDING)
                       .build())
               .toList();


       // 3. 메시지들을 Event Hub에 발송 요청 적재
       messages.forEach(message -> {
           MessageSendEvent sendEvent = MessageSendEvent.builder()
                   .messageId(message.getMessageId())
                   .messageGroupId(message.getMessageGroupId())
                   .content(message.getContent())
                   .recipientPhone(encryptionService.decrypt(
                           recipients.stream()
                                   .filter(r -> false)
                                   .findFirst()
                                   .get()
                                   .getEncryptedPhone()
                   ))
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
           recipientService.processRecipientFile(messageGroupId, file);
       } catch (Exception e) {
           log.error("Failed to upload recipients", e);
           throw new RuntimeException("Failed to upload recipients", e);
       }
   }

   @Override
   @Transactional
   public void processMessageResult(String messageId, String status) {
       Message message = messageRepository.findById(messageId)
               .orElseThrow(() -> new RuntimeException("Message not found"));

//       message.updateStatus(MessageStatus.valueOf(status));
//       messageRepository.save(message);
       messageRepository.delete(message); // EventHub에서 결과 적재 받으면 발송DB에서 삭제
   }

   @Override
   @Transactional(readOnly = true)
   public UploadProgressResponse getUploadProgress(String messageGroupId) {
       long totalCount = recipientRepository.countByMessageGroupId(messageGroupId);
       long successCount = recipientRepository.countByMessageGroupIdAndStatus(messageGroupId, "COMPLETED");
       long failCount = recipientRepository.countByMessageGroupIdAndStatus(messageGroupId, "FAILED");

       return UploadProgressResponse.builder()
               .processedCount((int)(successCount + failCount))
               .successCount((int)successCount)
               .failCount((int)failCount)
               .totalCount((int)totalCount)
               .status(totalCount == (successCount + failCount) ? "COMPLETED" : "PROCESSING")
               .build();
   }
}
