package com.ktds.rcsp.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // MessageGroup 캐시 설정
        Caffeine<Object, Object> messageGroupCaffeine = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)  // 1시간 후 만료
                .maximumSize(10000)                   // 최대 10000개 항목 저장
                .recordStats();                       // 캐시 통계 기록

        CaffeineCache messageGroupCache = new CaffeineCache("messageGroups", messageGroupCaffeine.build());

        // 기존 messageHistory 캐시 설정
        Caffeine<Object, Object> messageHistoryCaffeine = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10000)
                .recordStats();

        CaffeineCache messageHistoryCache = new CaffeineCache("messageHistory", messageHistoryCaffeine.build());

        // 모든 캐시 등록
        cacheManager.setCaches(Arrays.asList(messageGroupCache, messageHistoryCache));
        return cacheManager;
    }
}
