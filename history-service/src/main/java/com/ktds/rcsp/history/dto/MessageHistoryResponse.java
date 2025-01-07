package com.ktds.rcsp.history.dto;

import com.ktds.rcsp.history.domain.MessageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "메시지 이력 응답")
public class MessageHistoryResponse {
   
   @Schema(description = "메시지ID")
   private final String messageId;
   
   @Schema(description = "메시지그룹ID")
   private final String messageGroupId;

   @Schema(description = "마스터ID")
   private final String masterId;
   
   @Schema(description = "브랜드ID")
   private final String brandId;
   
   @Schema(description = "템플릿ID")
   private final String templateId;
   
   @Schema(description = "발신번호ID")
   private final String chatbotId;

   @Schema(description = "수신번호")
   private final String phoneNumber;
   
   @Schema(description = "메시지 내용")
   private final String content;
   
   @Schema(description = "상태")
   private final MessageStatus status;
   
   @Schema(description = "결과 코드")
   private final String resultCode;
   
   @Schema(description = "결과 메시지")
   private final String resultMessage;
   
   @Schema(description = "생성일시")
   private final LocalDateTime createdAt;
   
   @Schema(description = "수정일시")
   private final LocalDateTime updatedAt;
}
