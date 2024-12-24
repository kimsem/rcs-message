package com.ktds.rcsp.history.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageHistory {
   
   @Id
   private String messageId;
   
   @Column(name = "message_group_id", nullable = false)
   private String messageGroupId;
   
   @Column(name = "master_id", nullable = false)
   private String masterId;
   
   @Column(name = "brand_id", nullable = false)
   private String brandId;
   
   @Column(name = "template_id", nullable = false)
   private String templateId;
   
   @Column(name = "chatbot_id", nullable = false)
   private String chatbotId;
   
   @Column(nullable = false, length = 4000)
   private String content;
   
   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private MessageStatus status;
   
   @Column(name = "result_code")
   private String resultCode;
   
   @Column(name = "result_message")
   private String resultMessage;
   
   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;
   
   @Column(name = "updated_at")
   private LocalDateTime updatedAt;

   public void updateStatus(MessageStatus status, String resultCode, String resultMessage) {
       this.status = status;
       this.resultCode = resultCode;
       this.resultMessage = resultMessage;
       this.updatedAt = LocalDateTime.now();
   }
}
