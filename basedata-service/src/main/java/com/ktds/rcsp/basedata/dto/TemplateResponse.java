package com.ktds.rcsp.basedata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "템플릿 응답")
public class TemplateResponse {
   
   @Schema(description = "템플릿ID")
   private final String templateId;
   
   @Schema(description = "템플릿명")
   private final String templateName;
   
   @Schema(description = "템플릿 내용")
   private final String content;
}
