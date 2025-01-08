// message-service/src/main/java/com/ktds/rcsp/message/config/MetricsConfig.java
package com.ktds.rcsp.message.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MetricsConfig {

    // 파일 업로드 관련 메트릭
    @Bean
    public Timer uploadTimer(MeterRegistry registry) {
        return Timer.builder("file_upload_time")
                .description("File upload processing time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofSeconds(10),  // P95 목표: 10초
                        Duration.ofSeconds(20)   // P99 목표: 20초
                )
                .register(registry);
    }

    @Bean
    public Counter uploadTotalCounter(MeterRegistry registry) {
        return Counter.builder("file_upload_total")
                .description("Total number of file uploads")
                .register(registry);
    }

    // 암호화 처리 관련 메트릭
    @Bean
    public Timer encryptionTimer(MeterRegistry registry) {
        return Timer.builder("encryption_time")
                .description("Encryption processing time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(100),  // P95 목표: 100ms
                        Duration.ofMillis(200)   // P99 목표: 200ms
                )
                .register(registry);
    }

    @Bean
    public Counter encryptionTotalCounter(MeterRegistry registry) {
        return Counter.builder("encryption_total")
                .description("Total number of encryption requests")
                .register(registry);
    }

    // 메시지 발송 관련 메트릭
    @Bean
    public Timer messageSendTimer(MeterRegistry registry) {
        return Timer.builder("message_send_time")
                .description("Message sending time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(500),  // P95 목표: 500ms
                        Duration.ofSeconds(1)    // P99 목표: 1s
                )
                .register(registry);
    }

    @Bean
    public Counter messageSendTotalCounter(MeterRegistry registry) {
        return Counter.builder("message_send_total")
                .description("Total number of message send requests")
                .register(registry);
    }

    // Event Hub 관련 메트릭
    @Bean
    public Timer eventHubPublishTimer(MeterRegistry registry) {
        return Timer.builder("eventhub_publish_time")
                .description("Event Hub publish time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(100),  // P95 목표: 100ms
                        Duration.ofMillis(200)   // P99 목표: 200ms
                )
                .register(registry);
    }
}