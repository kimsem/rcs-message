package com.ktds.rcsp.common.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public abstract class Event {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final EventType eventType;

    protected Event(EventType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }
}
