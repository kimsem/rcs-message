package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "업로드 진행 상태 응답")
public class UploadProgressResponse {
   
   @Schema(description = "처리된 건수")
   private final int processedCount;
   
   @Schema(description = "성공 건수")
   private final int successCount;
   
   @Schema(description = "실패 건수")
   private final int failCount;
   
   @Schema(description = "전체 건수")
   private final int totalCount;
   
   @Schema(description = "처리 상태")
   private final String status;
}
