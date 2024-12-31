package com.ktds.rcsp.basedata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "발신번호 응답")
public class ChatbotResponse {
   
   @Schema(description = "발신번호ID")
   private final String chatbotId;
   
   @Schema(description = "전화번호")
   private final String phoneNumber;
}
