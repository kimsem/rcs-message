package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RecipientUploadEvent extends Event {
    private final String messageGroupId;
    private final String fileName;
    private final int totalCount;

    @Builder
    public RecipientUploadEvent(String messageGroupId, String fileName, int totalCount) {
        super(EventType.RECIPIENT_UPLOAD);
        this.messageGroupId = messageGroupId;
        this.fileName = fileName;
        this.totalCount = totalCount;
    }
}