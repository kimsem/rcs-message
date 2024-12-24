package com.ktds.rcsp.basedata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ktds.rcsp.basedata", "com.ktds.rcsp.common"})
public class BaseDataServiceApplication {
   public static void main(String[] args) {
       SpringApplication.run(BaseDataServiceApplication.class, args);
   }
}
