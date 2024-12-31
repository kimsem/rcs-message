package com.ktds.rcsp.message.infra.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class EventHubConfig {
   @Value("${azure.eventhub.connection-string}")
   private String connectionString;

   @Value("${azure.eventhub.encrypt-hub-name}")
   private String encryptHubName;

   @Value("${azure.eventhub.send-hub-name}")
   private String sendHubName;

   @Value("${azure.eventhub.result-hub-name}")
   private String resultHubName;

   @Bean(name = "encryptEventHubProducer")
   public EventHubProducerClient encryptEventHubProducer() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, encryptHubName)
              .buildProducerClient();
   }

   @Bean(name = "sendEventHubProducer")
   public EventHubProducerClient sendEventHubProducer() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, sendHubName)
              .buildProducerClient();
   }

   // Consumer Clients
   @Bean(name = "encryptEventHubConsumer")
   public EventHubConsumerClient encryptEventHubConsumer() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, encryptHubName)
              .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
              .buildConsumerClient();
   }

   @Bean(name = "sendEventHubConsumer")
   public EventHubConsumerClient sendEventHubConsumer() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, sendHubName)
              .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
              .buildConsumerClient();
   }

   @Bean(name = "resultEventHubConsumer")
   public EventHubConsumerClient resultEventHubConsumer() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, resultHubName)
              .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
              .buildConsumerClient();
   }

   // 에러 발생시 디버깅을 위한 설정 정보 출력
   @PostConstruct
   public void logConfig() {
      log.debug("EventHub Configuration:");
      log.info("Encrypt Hub Name: {}", encryptHubName);
      log.info("Send Hub Name: {}", sendHubName);
      log.info("Result Hub Name: {}", resultHubName);
      // 연결 문자열은 보안상 전체를 출력하지 않음
      log.info("Connection String configured: {}", connectionString != null);
   }
}