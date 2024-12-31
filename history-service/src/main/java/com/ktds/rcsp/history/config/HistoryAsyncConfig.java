package com.ktds.rcsp.history.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class HistoryAsyncConfig {
   
   @Bean(name = "historyTaskExecutor")
   public ThreadPoolTaskExecutor taskExecutor() {
       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
       executor.setCorePoolSize(5);
       executor.setMaxPoolSize(10);
       executor.setQueueCapacity(25);
       executor.setThreadNamePrefix("History-");
       executor.initialize();
       return executor;
   }
}
