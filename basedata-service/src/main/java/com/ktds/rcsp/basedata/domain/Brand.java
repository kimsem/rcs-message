package com.ktds.rcsp.basedata.domain;

import jakarta.persistence.*;
import lombok.*;

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
   
   @Column(nullable = false)
   private String status;
}
