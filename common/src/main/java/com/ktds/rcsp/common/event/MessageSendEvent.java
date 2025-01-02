package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageSendEvent extends Event {
    private final String messageId;
    private final String messageGroupId;
    private final String masterId;
    private final String brandId;
    private final String templateId;
    private final String chatbotId;
    private final String recipientPhone;
    private final String content;
    private final String status;

    @Builder
    public MessageSendEvent(String messageId, String messageGroupId, String masterId, String brandId, String templateId, String chatbotId, String content, String recipientPhone, String status) {
        super(EventType.MESSAGE_SEND);
        this.messageId = messageId;
        this.messageGroupId = messageGroupId;
        this.masterId = masterId;
        this.brandId = brandId;
        this.templateId = templateId;
        this.chatbotId = chatbotId;
        this.content = content;
        this.recipientPhone = recipientPhone;
        this.status = status;
    }

}

