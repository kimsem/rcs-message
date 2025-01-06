package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.event.MessageResultEvent;
import com.ktds.rcsp.common.event.MessageSendEvent;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.RecipientResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {
   MessageSendResponse sendMessage(MessageSendRequest request);
   void uploadRecipients(String messageGroupId, MultipartFile file);
   void processMessageResult(MessageResultEvent event);
   UploadProgressResponse getUploadProgress(String messageGroupId);
   void processMessageResultEvent(MessageSendEvent event);
}
