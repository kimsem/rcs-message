package com.ktds.rcsp.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 응답")
public class LoginResponse {
    @Schema(description = "액세스 토큰")
    private final String accessToken;
    
    @Schema(description = "리프레시 토큰")
    private final String refreshToken;

    @Schema(description = "마스터 ID")
    private final String masterId;

    @Schema(description = "사용자 ID")
    private final String userId;
}
