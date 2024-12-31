package com.ktds.rcsp.message.controller;

import com.ktds.rcsp.common.dto.ApiResponse;
import com.ktds.rcsp.message.domain.MessageGroupSummary;
import com.ktds.rcsp.message.domain.ProcessingStatus;
import com.ktds.rcsp.message.dto.MessageSendRequest;
import com.ktds.rcsp.message.dto.MessageSendResponse;
import com.ktds.rcsp.message.dto.UploadProgressResponse;
import com.ktds.rcsp.message.service.MessageService;
import com.ktds.rcsp.message.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "메시지", description = "메시지 발송 관련 API")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "메시지 발송", description = "RCS 메시지를 발송합니다")
    public ApiResponse<MessageSendResponse> sendMessage(@Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.success(messageService.sendMessage(request));
    }
}