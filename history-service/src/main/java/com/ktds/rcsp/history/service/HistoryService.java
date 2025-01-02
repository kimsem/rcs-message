package com.ktds.rcsp.history.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;

public interface HistoryService {
   PageResponse<MessageHistoryResponse> searchMessages(MessageHistorySearchRequest request);
   void saveMessageHistory(MessageSendEvent event);
   void updateMessageStatus(String messageId, String status, String resultCode, String resultMessage);
}
