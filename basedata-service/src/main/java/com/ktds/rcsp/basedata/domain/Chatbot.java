package com.ktds.rcsp.basedata.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Chatbot {

   @Id
   @Column(name = "chatbot_id")
   private String chatbotId;

   @Column(name = "brand_id", nullable = false)
   private String brandId;

   @Column(name = "phone_number", nullable = false)
   private String phoneNumber;

   @Column(nullable = false, length = 20)
   private String status;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   private LocalDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      createdAt = LocalDateTime.now();
   }

   @PreUpdate
   protected void onUpdate() {
      updatedAt = LocalDateTime.now();
   }
}
