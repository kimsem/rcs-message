package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메시지 발송 응답")
public class MessageSendResponse {
   
   @Schema(description = "메시지그룹ID")
   private final String messageGroupId;
   
   @Schema(description = "상태")
   private final String status;
}
