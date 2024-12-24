package com.ktds.rcsp.message.service;

import org.springframework.web.multipart.MultipartFile;

public interface RecipientService {
   void processRecipientFile(String messageGroupId, MultipartFile file);
   void encryptAndSaveRecipient(String messageGroupId, String phoneNumber);
}
