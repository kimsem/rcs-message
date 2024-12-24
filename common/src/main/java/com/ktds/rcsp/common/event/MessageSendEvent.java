package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageSendEvent extends Event {
    private final String messageId;
    private final String messageGroupId;
    private final String content;
    private final String recipientPhone;

    @Builder
    public MessageSendEvent(String messageId, String messageGroupId, String content, String recipientPhone) {
        super(EventType.MESSAGE_SEND);
        this.messageId = messageId;
        this.messageGroupId = messageGroupId;
        this.content = content;
        this.recipientPhone = recipientPhone;
    }
}

