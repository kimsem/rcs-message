package com.ktds.rcsp.history.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "message_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageHistory {

   @Id
   private String id;

   @Field("message_id")
   @Indexed
   private String messageId;

   @Field("message_group_id")
   @Indexed
   private String messageGroupId;

   @Field("master_id")
   @Indexed
   private String masterId;

   @Field("brand_id")
   @Indexed
   private String brandId;

   @Field("template_id")
   private String templateId;

   @Field("chatbot_id")
   @Indexed
   private String chatbotId;

   @Field("encrypted_phone")
   private String encryptedPhone;

   @Field("content")
   private String content;

   @Field("status")
   @Indexed
   private MessageStatus status;

   @Field("result_code")
   private String resultCode;

   @Field("result_message")
   private String resultMessage;

   @Field("created_at")
   @Indexed
   private LocalDateTime createdAt;

   @Field("updated_at")
   private LocalDateTime updatedAt;

   public void updateStatus(MessageStatus status, String resultCode, String resultMessage) {
      this.status = status;
      this.resultCode = resultCode;
      this.resultMessage = resultMessage;
      this.updatedAt = LocalDateTime.now();
   }
}