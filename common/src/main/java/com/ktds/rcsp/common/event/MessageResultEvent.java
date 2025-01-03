package com.ktds.rcsp.common.event;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageResultEvent extends Event {
    private final String messageId;
    private final String status;
    private final String resultCode;
    private final String resultMessage;

    @Builder
    public MessageResultEvent(@JsonProperty("messageId") String messageId,
                              @JsonProperty("status") String status,
                              @JsonProperty("resultCode") String resultCode,
                              @JsonProperty("resultMessage") String resultMessage) {
        super(EventType.MESSAGE_SEND);
        this.messageId = messageId;
        this.status = status;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}