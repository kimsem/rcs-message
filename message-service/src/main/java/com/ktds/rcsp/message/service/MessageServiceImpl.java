package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.message.domain.Message;
import com.ktds.rcsp.message.domain.MessageStatus;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.common.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

   private final MessageRepository messageRepository;
   private final RecipientRepository recipientRepository;
   private final EventHubMessagePublisher eventPublisher;
   private final RecipientService recipientService;

   @Override
   @Transactional
   public MessageSendResponse sendMessage(MessageSendRequest request) {
       Message message = Message.builder()
               .messageId(UUID.randomUUID().toString())
               .messageGroupId(request.getMessageGroupId())
               .brandId(request.getBrandId())
               .templateId(request.getTemplateId())
               .chatbotId(request.getChatbotId())
               .content(request.getContent())
               .status(MessageStatus.PENDING)
               .build();

       messageRepository.save(message);

       eventPublisher.publishMessageSendEvent(MessageSendEvent.builder()
               .messageId(message.getMessageId())
               .messageGroupId(message.getMessageGroupId())
               .content(message.getContent())
               .build());

       return MessageSendResponse.builder()
               .messageGroupId(message.getMessageGroupId())
               .status(message.getStatus().name())
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
       
       message.updateStatus(MessageStatus.valueOf(status));
       messageRepository.save(message);
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
