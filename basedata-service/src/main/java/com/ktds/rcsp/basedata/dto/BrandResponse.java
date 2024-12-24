package com.ktds.rcsp.basedata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "브랜드 응답")
public class BrandResponse {
   
   @Schema(description = "브랜드ID")
   private final String brandId;
   
   @Schema(description = "브랜드명")
   private final String brandName;
}
