package com.ktds.rcsp.history.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;

import java.util.List;

public interface HistoryService {
   PageResponse<MessageHistoryResponse> searchMessages(MessageHistorySearchRequest request);
   void saveMessageHistory(List<MessageSendEvent> event);
   void updateMessageStatus(MessageResultEvent event);
}
