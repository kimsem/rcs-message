package com.ktds.rcsp.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청")
public class LoginRequest {
    @NotBlank(message = "마스터 ID는 필수입니다")
    @Schema(description = "마스터 ID", example = "MASTER001")
    private String masterId;
    
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "USER001")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호")
    private String password;
}
