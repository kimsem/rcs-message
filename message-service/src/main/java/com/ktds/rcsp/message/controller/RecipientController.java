package com.ktds.rcsp.message.controller;

import com.ktds.rcsp.common.dto.ApiResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.service.RecipientService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/recipients")
@RequiredArgsConstructor
@Tag(name = "수신자", description = "수신자 정보 처리 관련 API")
public class RecipientController {

    private final RecipientService recipientService;

    @PostMapping("/upload")
    @Operation(summary = "수신자 정보 업로드", description = "수신자 정보 파일을 업로드합니다")
    public ApiResponse<Void> uploadRecipients(
            @RequestParam String messageGroupId,
            @RequestParam String brandId,
            @RequestParam String templateId,
            @RequestParam String chatbotId,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Starting file upload for messageGroupId: {}, filename: {}",
                    messageGroupId, file.getOriginalFilename());

            String masterId = SecurityContextHolder.getContext()
                    .getAuthentication().getName();

            recipientService.processRecipientFile(messageGroupId, file, masterId, brandId, templateId, chatbotId);

            log.info("File upload initiated successfully for messageGroupId: {}", messageGroupId);
            return ApiResponse.success(null);

        } catch (Exception e) {
            log.error("Error during file upload for messageGroupId: {}", messageGroupId, e);
            throw e;
        }
    }

    @GetMapping("/status")
    @Operation(summary = "업로드 진행 상태 조회", description = "수신자 정보 업로드 진행 상태를 조회합니다")
    public ApiResponse<UploadProgressResponse> getUploadProgress(
            @RequestParam String messageGroupId) {
        try {
            log.debug("Fetching upload progress for messageGroupId: {}", messageGroupId);

            UploadProgressResponse response = recipientService.getUploadProgress(messageGroupId);

            log.debug("Upload progress for messageGroupId: {}, status: {}, processed: {}/{}",
                    messageGroupId, response.getStatus(),
                    response.getProcessedCount(), response.getTotalCount());

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error fetching upload progress for messageGroupId: {}", messageGroupId, e);
            throw e;
        }
    }

    @GetMapping("/download/template")
    @Operation(summary = "수신자 정보 템플릿 다운로드", description = "수신자 정보 입력용 템플릿 파일을 다운로드합니다")
    public ApiResponse<String> downloadTemplate() {
        log.info("Template download requested");
        return ApiResponse.success("template.xlsx");  // 템플릿 파일 URL 또는 Base64 인코딩된 파일 내용 반환
    }

    @GetMapping("/validation")
    @Operation(summary = "파일 유효성 검사", description = "업로드된 파일의 형식과 내용이 올바른지 검사합니다")
    public ApiResponse<Boolean> validateFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("File validation requested for: {}", file.getOriginalFilename());
            // 파일 유효성 검사 로직 구현 필요
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("File validation failed for: {}", file.getOriginalFilename(), e);
            return ApiResponse.success(false);
        }
    }
}
