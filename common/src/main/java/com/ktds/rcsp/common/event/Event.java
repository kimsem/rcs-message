package com.ktds.rcsp.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public abstract class Event {
    @JsonProperty("eventId")
    private final String eventId;

    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;

    @JsonProperty("eventType")
    private final EventType eventType;

    // 기본 생성자 추가
    protected Event() {
        this.eventId = null;
        this.timestamp = null;
        this.eventType = null;
    }

    protected Event(EventType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }
}