package com.ktds.rcsp.common.event.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.springframework.beans.factory.annotation.Value;
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
   public EventHubProducerClient numberEncryptProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, numberEncryptEventHub)
              .buildProducerClient();
   }

   @Bean
   public EventHubProducerClient messageSendProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .buildProducerClient();
   }

   @Bean
   public EventHubProducerClient messageResultProducerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .buildProducerClient();
   }

   // Async Consumer Clients
   @Bean
   public EventHubConsumerAsyncClient numberEncryptConsumerAsyncClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, numberEncryptEventHub)
              .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
              .buildAsyncConsumerClient();
   }


   @Bean(name = "messageResultMessageConsumer")
   public EventHubConsumerAsyncClient messageResultConsumerAsyncClientForMessage(
           @Value("${azure.eventhub.consumer-group.message}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

   @Bean(name = "messageResultHistoryConsumer")
   public EventHubConsumerAsyncClient messageResultConsumerAsyncClientForHistory(
           @Value("${azure.eventhub.consumer-group.history}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageResultEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

   @Bean(name = "messageSendMessageConsumer")
   public EventHubConsumerAsyncClient messageSendConsumerAsyncClientForMessage(
           @Value("${azure.eventhub.consumer-group.message}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

   @Bean(name = "messageSendHistoryConsumer")
   public EventHubConsumerAsyncClient messageSendConsumerAsyncClientForHistory(
           @Value("${azure.eventhub.consumer-group.history}") String consumerGroup) {
      return new EventHubClientBuilder()
              .connectionString(connectionString, messageSendEventHub)
              .consumerGroup(consumerGroup)
              .buildAsyncConsumerClient();
   }

}
