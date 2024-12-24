package com.ktds.rcsp.common.event;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class RecipientEncryptEvent extends Event {
    private final String messageGroupId;
    private final List<String> phoneNumbers;

    @Builder
    public RecipientEncryptEvent(String messageGroupId, List<String> phoneNumbers) {
        super(EventType.RECIPIENT_ENCRYPT);
        this.messageGroupId = messageGroupId;
        this.phoneNumbers = phoneNumbers;
    }
}