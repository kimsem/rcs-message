package com.ktds.rcsp.basedata.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Brand {

   @Id
   @Column(name = "brand_id")
   private String brandId;

   @Column(name = "master_id", nullable = false)
   private String masterId;

   @Column(name = "brand_name", nullable = false)
   private String brandName;

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
