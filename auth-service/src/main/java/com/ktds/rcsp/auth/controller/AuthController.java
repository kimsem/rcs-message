package com.ktds.rcsp.auth.controller;

import com.ktds.rcsp.auth.dto.LoginRequest;
import com.ktds.rcsp.auth.dto.LoginResponse;
import com.ktds.rcsp.auth.service.AuthService;
import com.ktds.rcsp.common.dto.ApiResponse;
import com.ktds.rcsp.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = authService.login(request);
            log.debug("Login successful for user: {}", request.getUserId());
            return ApiResponse.success(loginResponse);
        } catch (BusinessException e) {
            log.error("Login failed for user: {}", request.getUserId(), e);
            throw e;
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다")
    public ApiResponse<Boolean> verifyToken(@RequestParam String token) {
        return ApiResponse.success(authService.verifyToken(token));
    }
}