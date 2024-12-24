package com.ktds.rcsp.history.event;

import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

   private final HistoryService historyService;

   @Async("historyTaskExecutor")
   @TransactionalEventListener
   public void handleMessageSendEvent(MessageSendEvent event) {
       try {
           // 이력 저장 로직
           log.info("Received message send event: {}", event.getMessageId());
       } catch (Exception e) {
           log.error("Failed to handle message send event", e);
       }
   }
}

