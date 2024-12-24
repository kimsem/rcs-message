package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "수신자 정보 업로드 요청")
public class RecipientUploadRequest {

   @Schema(description = "메시지그룹ID")
   private String messageGroupId;
}
