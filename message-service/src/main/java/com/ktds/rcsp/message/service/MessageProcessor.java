package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.dto.ErrorCode;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.common.exception.BusinessException;
import com.ktds.rcsp.message.domain.Message;
import com.ktds.rcsp.message.domain.MessageGroup;
import com.ktds.rcsp.message.domain.MessageStatus;
import com.ktds.rcsp.message.domain.Recipient;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.infra.EncryptionService;
import com.ktds.rcsp.message.infra.EventHubMessagePublisher;
import com.ktds.rcsp.message.repository.MessageGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessor {

    private final EncryptionService encryptionService;
    private final EventHubMessagePublisher eventPublisher;
    private final MessageGroupRepository messageGroupRepository;
    private static final int BATCH_SIZE = 1000;

    @Async
    public void processMessagesAsync(MessageSendRequest request, List<Recipient> recipients, MessageGroup messageGroup) {
        for (int i = 0; i < recipients.size(); i += BATCH_SIZE) {
            List<Recipient> batchRecipients = recipients.subList(i, Math.min(i + BATCH_SIZE, recipients.size()));

            List<Message> messagesBatch = batchRecipients.stream()
                    .map(recipient -> Message.builder()
                            .messageId(UUID.randomUUID().toString())
                            .messageGroup(messageGroup)
                            .recipientId(recipient.getEncryptedPhone())
                            .content(request.getContent())
                            .status(MessageStatus.PENDING)
                            .build())
                    .toList();

            List<MessageSendEvent> sendEventsBatch = messagesBatch.stream()
                    .map(message -> MessageSendEvent.builder()
                            .messageId(message.getMessageId())
                            .messageGroupId(messageGroup.getMessageGroupId())
                            .masterId(messageGroup.getMasterId())
                            .brandId(message.getMessageGroup().getBrandId())
                            .templateId(message.getMessageGroup().getTemplateId())
                            .chatbotId(message.getMessageGroup().getChatbotId())
                            .content(message.getContent())
                            .recipientPhone(encryptionService.decrypt(message.getRecipientId()))
                            .status(MessageStatus.PENDING.name())
                            .build())
                    .collect(Collectors.toList());

            eventPublisher.publishSendEvent(sendEventsBatch);
        }
    }

    @Cacheable(cacheNames = "messageGroups", key = "#messageGroupId")
    public MessageGroup getMessageGroup(String messageGroupId) {
        return messageGroupRepository.findById(messageGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_GROUP_NOT_FOUND));
    }
}
