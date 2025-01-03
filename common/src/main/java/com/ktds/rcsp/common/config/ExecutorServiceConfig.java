package com.ktds.rcsp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);  // 10개의 스레드를 사용하는 ExecutorService 빈 생성
    }
}
