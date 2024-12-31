package com.ktds.rcsp.basedata.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Template {
   
   @Id
   @Column(name = "template_id")
   private String templateId;
   
   @Column(name = "brand_id", nullable = false)
   private String brandId;
   
   @Column(name = "template_name", nullable = false)
   private String templateName;
   
   @Column(nullable = false, length = 4000)
   private String content;
   
   @Column(nullable = false)
   private String status;
}
