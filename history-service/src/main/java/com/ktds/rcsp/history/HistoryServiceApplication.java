package com.ktds.rcsp.history;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ktds.rcsp.history", "com.ktds.rcsp.common"})
public class HistoryServiceApplication {
   public static void main(String[] args) {
       SpringApplication.run(HistoryServiceApplication.class, args);
   }
}
