package com.ktds.rcsp.history.infra;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.rcsp.history.service.HistoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubHistorySubscriber {
    private final EventHubConsumerAsyncClient messageSendHistoryConsumer;
    private final EventHubConsumerAsyncClient messageResultHistoryConsumer;
    private final ObjectMapper objectMapper;
    private final HistoryService historyService;

    @PostConstruct
    public void subscribe() {

    }
}
