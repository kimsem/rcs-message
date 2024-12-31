package com.ktds.rcsp.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class MessageResultEvent extends Event {
    private final String messageId;
    private final String status;
    private final String resultCode;
    private final String resultMessage;
    private final String messageGroupId;  // 추가

    @JsonCreator
    public MessageResultEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("messageId") String messageId,
            @JsonProperty("status") String status,
            @JsonProperty("resultCode") String resultCode,
            @JsonProperty("resultMessage") String resultMessage,
            @JsonProperty("messageGroupId") String messageGroupId  // 추가
    ) {
        super(eventType);
        this.messageId = messageId;
        this.status = status;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.messageGroupId = messageGroupId;  // 추가
    }

    // 기존 빌더 메서드 유지 또는 필요에 따라 수정
    public static MessageResultEvent.Builder builder() {
        return new MessageResultEvent.Builder();
    }

    public static class Builder {
        private String eventId;
        private LocalDateTime timestamp;
        private EventType eventType;
        private String messageId;
        private String status;
        private String resultCode;
        private String resultMessage;
        private String messageGroupId;  // 추가

        // 빌더 메서드들 추가 (기존과 동일한 방식)

        public MessageResultEvent build() {
            return new MessageResultEvent(
                    eventId,
                    timestamp,
                    eventType,
                    messageId,
                    status,
                    resultCode,
                    resultMessage,
                    messageGroupId
            );
        }
    }
}