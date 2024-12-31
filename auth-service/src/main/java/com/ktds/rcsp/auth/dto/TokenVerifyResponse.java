package com.ktds.rcsp.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "토큰 검증 응답")
public class TokenVerifyResponse {

    @Schema(description = "토큰 유효성 여부", example = "true")
    private final boolean isValid;

    @Schema(description = "사용자 ID", example = "USER001")
    private final String userId;
}