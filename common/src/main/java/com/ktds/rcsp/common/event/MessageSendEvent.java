package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageSendEvent extends Event {
    private final String messageId;
    private final String messageGroupId;
    private final String content;

    @Builder
    public MessageSendEvent(String messageId, String messageGroupId, String content) {
        super("MESSAGE_SEND");
        this.messageId = messageId;
        this.messageGroupId = messageGroupId;
        this.content = content;
    }
}
