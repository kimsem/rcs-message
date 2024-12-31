package com.ktds.rcsp.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "정렬 요청")
public class SortRequest {
   
   @Schema(description = "정렬 필드")
   private String field;
   
   @Schema(description = "정렬 방향")
   private String direction;
}
