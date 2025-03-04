package com.ktds.rcsp.auth.service;

import com.ktds.rcsp.auth.domain.User;
import com.ktds.rcsp.auth.dto.LoginRequest;
import com.ktds.rcsp.auth.dto.LoginResponse;
import com.ktds.rcsp.auth.repository.UserRepository;
import com.ktds.rcsp.common.dto.ErrorCode;
import com.ktds.rcsp.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;


    @PostConstruct  // 서비스 시작시 테스트
    public void testPasswordEncoder() {
        String rawPassword = "1234";
        String encoded = passwordEncoder.encode(rawPassword);
        log.debug("Test encode result: {}", encoded);
        log.debug("Test matches result: {}", passwordEncoder.matches(rawPassword, encoded));
    }


    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByMasterIdAndUserId(request.getMasterId(), request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새로운 해시 생성해서 비교
        String newHash = passwordEncoder.encode(request.getPassword());
        log.debug("New hash for input password: {}", newHash);
        log.debug("Input password: {}", request.getPassword());
        log.debug("Stored password hash: {}", user.getPassword());
        log.debug("Password match result: {}",
                passwordEncoder.matches(request.getPassword(), user.getPassword()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createToken(user.getMasterId(), user.getUserId());
        String refreshToken = jwtTokenProvider.createToken(user.getMasterId(), user.getUserId());

        // 토큰 발급 시 MASTER_ID와 USER_ID도 포함하여 응답
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .masterId(user.getMasterId())
                .userId(user.getUserId())
                .build();
    }

    @Override
    public boolean verifyToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
