package com.ktds.rcsp.message.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "message_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageHistory {
    @Id
    private String id;

    private String messageId;
    private String messageGroupId;
    private String masterId;
    private String brandId;
    private String templateId;
    private String chatbotId;
    private String encryptedPhone;
    private String content;
    private String status;
    private String resultCode;
    private String resultMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
