package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessageService {
   MessageSendResponse sendMessage(MessageSendRequest request);
   void uploadRecipients(String messageGroupId, String masterId, MultipartFile file);
   void processMessageResult(MessageResultEvent event);
   UploadProgressResponse getUploadProgress(String messageGroupId);
   void processMessageResultEvent(List<MessageSendEvent> event);
}
