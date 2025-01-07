package com.ktds.rcsp.history.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer historySearchTimer(MeterRegistry registry) {
        return Timer.builder("history_search_time")
                .description("History search response time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(500),  // P95 목표
                        Duration.ofSeconds(1)    // P99 목표
                )
                .register(registry);
    }

    @Bean
    public Counter historySearchTotalCounter(MeterRegistry registry) {
        return Counter.builder("history_search_total")
                .description("Total number of history search requests")
                .register(registry);
    }

    @Bean
    public Counter historySearchErrorCounter(MeterRegistry registry) {
        return Counter.builder("history_search_errors")
                .description("Total number of history search errors")
                .register(registry);
    }

    @Bean
    public Counter dbConnectionCounter(MeterRegistry registry) {
        return Counter.builder("db_connections_total")
                .description("Total number of DB connections")
                .register(registry);
    }

    @Bean
    public Timer dbQueryTimer(MeterRegistry registry) {
        return Timer.builder("db_query_time")
                .description("Database query execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(100),  // P95 목표
                        Duration.ofMillis(200)   // P99 목표
                )
                .register(registry);
    }
}