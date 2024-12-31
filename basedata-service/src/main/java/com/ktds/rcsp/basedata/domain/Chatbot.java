package com.ktds.rcsp.basedata.domain;

import jakarta.persistence.*;
import lombok.*;

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
   
   @Column(nullable = false)
   private String status;
}
