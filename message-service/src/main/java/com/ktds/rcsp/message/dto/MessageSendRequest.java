package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메시지 발송 요청")
public class MessageSendRequest {

   @NotBlank(message = "브랜드ID는 필수입니다")
   @Schema(description = "브랜드ID")
   private String brandId;

   @NotBlank(message = "템플릿ID는 필수입니다")
   @Schema(description = "템플릿ID")
   private String templateId;

   @NotBlank(message = "발신번호ID는 필수입니다")
   @Schema(description = "발신번호ID")
   private String chatbotId;

   @NotBlank(message = "메시지그룹ID는 필수입니다")
   @Schema(description = "메시지그룹ID")
   private String messageGroupId;

   @NotBlank(message = "메시지 내용은 필수입니다")
   @Schema(description = "메시지 내용")
   private String content;
}