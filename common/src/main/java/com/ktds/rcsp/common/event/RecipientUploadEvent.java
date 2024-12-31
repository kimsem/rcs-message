package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RecipientUploadEvent extends Event {
    private final String messageGroupId;
    private final String phoneNumber;

    @Builder
    public RecipientUploadEvent(String messageGroupId, String phoneNumber) {
        super(EventType.RECIPIENT_UPLOAD);  // EventType enum에 정의된 값 사용
        this.messageGroupId = messageGroupId;
        this.phoneNumber = phoneNumber;
    }
}