package com.ktds.rcsp.common.event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageResultEvent extends Event {
    private final String messageId;
    private final String status;
    private final String resultCode;
    private final String resultMessage;

    @Builder
    public MessageResultEvent(String messageId, String status, String resultCode, String resultMessage) {
        super(EventType.MESSAGE_RESULT);
        this.messageId = messageId;
        this.status = status;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}