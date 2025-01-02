package com.ktds.rcsp.message.service;

import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {
   MessageSendResponse sendMessage(MessageSendRequest request);
   void uploadRecipients(String messageGroupId, MultipartFile file);
   void processMessageResult(String messageId, String status);
   UploadProgressResponse getUploadProgress(String messageGroupId);
}