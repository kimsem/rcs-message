package com.ktds.rcsp.auth.service;

import com.ktds.rcsp.auth.dto.LoginRequest;
import com.ktds.rcsp.auth.dto.LoginResponse;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

@Validated
public interface AuthService {
    LoginResponse login(@Valid LoginRequest request);
    boolean verifyToken(String token);
}
