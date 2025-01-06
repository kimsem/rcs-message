package com.ktds.rcsp.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "메시지 이력 검색 요청")
public class MessageHistorySearchRequest {
   
   @Schema(description = "시작일시")
   private LocalDateTime startDate;
   
   @Schema(description = "종료일시")
   private LocalDateTime endDate;
   
   @Schema(description = "브랜드ID")
   private String brandId;
   
   @Schema(description = "발신번호ID")
   private String chatbotId;
   
   @Schema(description = "메시지그룹ID")
   private String messageGroupId;

   @Schema(description = "마스터ID")
   private String masterId;
   
   @Schema(description = "상태")
   private String status;
   
   @Schema(description = "페이지 번호")
   private int page;
   
   @Schema(description = "페이지 크기")
   private int size;
}

