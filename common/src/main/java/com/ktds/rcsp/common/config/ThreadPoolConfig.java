package com.ktds.rcsp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ExecutorService executorService() {
        int corePoolSize = Runtime.getRuntime().availableProcessors(); // 최소 스레드
        int maxPoolSize = corePoolSize * 4; // 최대 스레드 (I/O 집약적 작업 고려)
        long keepAliveTime = 60L; // 비활성 스레드 유지 시간 (초)

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // 작업 큐 크기 제한
                new ThreadPoolExecutor.CallerRunsPolicy() // 큐가 꽉 찼을 때 호출자 실행
        );
    }
}
