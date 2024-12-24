package com.ktds.rcsp.message.infra.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHubConfig {
   
   @Value("${azure.eventhub.connection-string}")
   private String connectionString;
   
   @Value("${azure.eventhub.name}")
   private String eventHubName;
   
   @Bean
   public EventHubProducerClient eventHubProducerClient() {
       return new EventHubClientBuilder()
               .connectionString(connectionString, eventHubName)
               .buildProducerClient();
   }

   @Bean
   public EventHubConsumerClient eventHubConsumerClient() {
      return new EventHubClientBuilder()
              .connectionString(connectionString, eventHubName)
              .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
              .buildConsumerClient();
   }

}
