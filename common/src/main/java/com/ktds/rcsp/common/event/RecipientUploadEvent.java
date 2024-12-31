package com.ktds.rcsp.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RecipientUploadEvent extends Event {
    private final String messageGroupId;
    private final String phoneNumber;
    private final String name;
    private final String status;

    // 기본 생성자 추가
    public RecipientUploadEvent() {
        super(EventType.RECIPIENT_UPLOAD);
        this.messageGroupId = null;
        this.phoneNumber = null;
        this.name = null;
        this.status = null;
    }

    // JSON 직렬화/역직렬화를 위한 생성자
    @JsonCreator
    public RecipientUploadEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("timestamp") java.time.LocalDateTime timestamp,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("messageGroupId") String messageGroupId,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status) {
        super(EventType.RECIPIENT_UPLOAD);
        this.messageGroupId = messageGroupId;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.status = status;
    }

    // 빌더 패턴을 위한 생성자
    @Builder
    public RecipientUploadEvent(
            String messageGroupId,
            String phoneNumber,
            String name,
            String status
    ) {
        super(EventType.RECIPIENT_UPLOAD);
        this.messageGroupId = messageGroupId;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.status = status;
    }
}