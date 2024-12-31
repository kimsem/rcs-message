package com.ktds.rcsp.history;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ktds.rcsp.history", "com.ktds.rcsp.common"})
@EntityScan(basePackages = {"com.ktds.rcsp.history.domain", "com.ktds.rcsp.common.domain"})
@EnableJpaRepositories(basePackages = {"com.ktds.rcsp.history.repository"})
public class HistoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HistoryServiceApplication.class, args);
    }
}