package com.ktds.rcsp.common.event.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHubConfig {

   @Value("${azure.eventhub.connection-string}")
   private String connectionString;

   @Value("${azure.eventhub.number-encrypt.name}")
   private String numberEncryptEventHub;

   @Value("${azure.eventhub.message-send.name}")
   private String messageSendEventHub;

   @Value("${azure.eventhub.message-result.name}")
   private String messageResultEventHub;

   // Producer Clients
   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.number-encrypt.producer.enabled", havingValue = "true")
   public EventHubProducerClient numberEncryptProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, numberEncryptEventHub)
              .buildProducerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-send.producer.enabled", havingValue = "true")
   public EventHubProducerClient messageSendProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .buildProducerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-result.producer.enabled", havingValue = "true")
   public EventHubProducerClient messageResultProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .buildProducerClient();
   }

   // Consumer Clients
   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.number-encrypt.consumer.enabled", havingValue = "true")
   public EventHubConsumerClient numberEncryptConsumerClient(
           @Value("${azure.eventhub.number-encrypt.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, numberEncryptEventHub)
              .consumerGroup(consumerGroup)
              .buildConsumerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-send.consumer.enabled", havingValue = "true")
   public EventHubConsumerClient messageSendConsumerClient(
           @Value("${azure.eventhub.message-send.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .consumerGroup(consumerGroup)
              .buildConsumerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-result.consumer.enabled", havingValue = "true")
   public EventHubConsumerClient messageResultConsumerClient(
           @Value("${azure.eventhub.message-result.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .consumerGroup(consumerGroup)
              .buildConsumerClient();
   }

   // Async Consumer Clients
   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.number-encrypt.consumer.enabled", havingValue = "true")
   public EventHubConsumerAsyncClient numberEncryptConsumerAsyncClient(
           @Value("${azure.eventhub.number-encrypt.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, numberEncryptEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-send.consumer.enabled", havingValue = "true")
   public EventHubConsumerAsyncClient messageSendConsumerAsyncClient(
           @Value("${azure.eventhub.message-send.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

   @Bean
   @ConditionalOnProperty(name = "azure.eventhub.message-result.consumer.enabled", havingValue = "true")
   public EventHubConsumerAsyncClient messageResultConsumerAsyncClient(
           @Value("${azure.eventhub.message-result.consumer-group}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

}
