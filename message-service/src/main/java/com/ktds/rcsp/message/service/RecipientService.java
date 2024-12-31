package com.ktds.rcsp.message.service;

import com.ktds.rcsp.message.dto.UploadProgressResponse;
import org.springframework.web.multipart.MultipartFile;

public interface RecipientService {
   void processRecipientFile(String messageGroupId, MultipartFile file);
   UploadProgressResponse getUploadProgress(String messageGroupId);  // 상태 조회 메서드 추가
}