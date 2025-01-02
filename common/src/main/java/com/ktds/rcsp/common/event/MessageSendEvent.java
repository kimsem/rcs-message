package com.ktds.rcsp.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageSendEvent extends Event {
    @JsonProperty("messageId")
    private final String messageId;
    private final String messageGroupId;
    private final String masterId;
    private final String brandId;
    private final String templateId;
    private final String chatbotId;
    private final String recipientPhone;
    private final String content;
    private final String status;


    @Builder
    public MessageSendEvent(@JsonProperty("messageId") String messageId,
                            @JsonProperty("messageGroupId") String messageGroupId,
                            @JsonProperty("masterId") String masterId,
                            @JsonProperty("brandId") String brandId,
                            @JsonProperty("templateId") String templateId,
                            @JsonProperty("chatbotId") String chatbotId,
                            @JsonProperty("recipientPhone") String recipientPhone,
                            @JsonProperty("content") String content,
                            @JsonProperty("status") String status) {
        super(EventType.MESSAGE_SEND);
        this.messageId = messageId;
        this.messageGroupId = messageGroupId;
        this.masterId = masterId;
        this.brandId = brandId;
        this.templateId = templateId;
        this.chatbotId = chatbotId;
        this.recipientPhone = recipientPhone;
        this.content = content;
        this.status = status;
    }

}

