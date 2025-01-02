package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "수신자 정보 업로드 응답")
public class RecipientUploadResponse {

   @Schema(description = "메시지그룹ID")
   private final String messageGroupId;

   @Schema(description = "업로드 상태")
   private final boolean status;
}