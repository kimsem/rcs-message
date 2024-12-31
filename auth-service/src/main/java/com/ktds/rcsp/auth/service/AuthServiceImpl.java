package com.ktds.rcsp.auth.service;

import com.ktds.rcsp.auth.domain.User;
import com.ktds.rcsp.auth.dto.LoginRequest;
import com.ktds.rcsp.auth.dto.LoginResponse;
import com.ktds.rcsp.auth.repository.UserRepository;
import com.ktds.rcsp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByMasterIdAndUserId(request.getMasterId(), request.getUserId())
                .orElseThrow(() -> new BusinessException("사용자 정보를 찾을 수 없습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenProvider.createToken(user.getMasterId(), user.getUserId());
        String refreshToken = jwtTokenProvider.createToken(user.getMasterId(), user.getUserId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public boolean verifyToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}