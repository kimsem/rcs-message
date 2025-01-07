package com.ktds.rcsp.message.service;

import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.common.event.RecipientUploadEvent;
import com.ktds.rcsp.message.domain.MessageGroup;
import com.ktds.rcsp.message.dto.RecipientResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecipientService {
   void processRecipientFile(String messageGroupId, MultipartFile file);
   void encryptAndSaveRecipient(List<RecipientUploadEvent> events);

   PageResponse<RecipientResponse> searchRecipients(String messageGroupId, Pageable pageable);
}
