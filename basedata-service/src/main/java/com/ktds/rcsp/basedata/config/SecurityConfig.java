package com.ktds.rcsp.basedata.config;

import com.ktds.rcsp.common.config.CommonSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@Import(CommonSecurityConfig.class)  // 공통 설정 임포트
public class SecurityConfig {
    // 서비스별 고유한 보안 설정만 유지
}