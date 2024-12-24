package com.ktds.rcsp.auth.controller;

import com.ktds.rcsp.auth.dto.LoginRequest;
import com.ktds.rcsp.auth.dto.LoginResponse;
import com.ktds.rcsp.auth.service.AuthService;
import com.ktds.rcsp.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/verify")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다")
    public ApiResponse<Boolean> verifyToken(@RequestParam String token) {
        return ApiResponse.success(authService.verifyToken(token));
    }
}
