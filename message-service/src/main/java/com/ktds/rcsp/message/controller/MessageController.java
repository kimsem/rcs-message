package com.ktds.rcsp.message.controller;

import com.ktds.rcsp.common.dto.ApiResponse;
import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.RecipientResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.service.MessageService;
import com.ktds.rcsp.message.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "메시지", description = "메시지 발송 관련 API")
public class MessageController {

   private final MessageService messageService;
    private final RecipientService recipientService;

    @PostMapping("/send")
   @Operation(summary = "메시지 발송", description = "RCS 메시지를 발송합니다")
   public ApiResponse<MessageSendResponse> sendMessage(@Valid @RequestBody MessageSendRequest request) {
       return ApiResponse.success(messageService.sendMessage(request));
   }

   @PostMapping(path = "/recipients/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @Operation(summary = "수신자 정보 업로드", description = "수신자 정보 파일을 업로드합니다")
   public ApiResponse<Void> uploadRecipients(
           @RequestParam String messageGroupId,
           @RequestParam String masterId,
           @RequestPart("file") MultipartFile file) {
       messageService.uploadRecipients(messageGroupId, masterId, file);
       return ApiResponse.success(null);
   }

   @GetMapping("/recipients/status")
   @Operation(summary = "업로드 진행 상태 조회", description = "수신자 정보 업로드 진행 상태를 조회합니다")
   public ApiResponse<UploadProgressResponse> getUploadProgress(
           @RequestParam String messageGroupId) {
       return ApiResponse.success(messageService.getUploadProgress(messageGroupId));
   }

    @GetMapping("/recipients/search")
    @Operation(summary = "수신자 정보 조회", description = "메시지 그룹 ID로 수신자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<RecipientResponse>>> searchRecipients(
            @RequestParam String messageGroupId,
            Pageable pageable) {
        PageResponse<RecipientResponse> recipients = recipientService.searchRecipients(messageGroupId, pageable);
        return ResponseEntity.ok(ApiResponse.success(recipients));
    }
}
