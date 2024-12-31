package com.ktds.rcsp.message.service;

import com.ktds.rcsp.message.domain.MessageGroupSummary;
import com.ktds.rcsp.message.repository.MessageGroupSummaryRepository;
import com.ktds.rcsp.message.domain.Message;
import com.ktds.rcsp.message.domain.MessageStatus;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageRepository;
import com.ktds.rcsp.message.repository.RecipientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import java.util.UUID;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final EventHubMessagePublisher eventPublisher;

    @Autowired
    public MessageServiceImpl(
            MessageRepository messageRepository,
            EventHubMessagePublisher eventPublisher) {
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

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
    public void processMessageResult(String messageId, String status) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.updateStatus(MessageStatus.valueOf(status));
        messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public UploadProgressResponse getUploadProgress(String messageGroupId) {
        // Default values for an empty response
        return UploadProgressResponse.builder()
                .totalCount(0)
                .processedCount(0)
                .successCount(0)
                .failCount(0)
                .status("PENDING")
                .build();
    }

    @Override
    public void uploadRecipients(String messageGroupId, MultipartFile file) {
        // This method should be empty or throw UnsupportedOperationException
        // as it's not part of message processing responsibility
        throw new UnsupportedOperationException("Recipient upload is not supported in MessageService");
    }
}