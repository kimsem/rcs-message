package com.ktds.rcsp.history.controller;

import com.ktds.rcsp.common.dto.ApiResponse;
import com.ktds.rcsp.common.dto.PageResponse;
import com.ktds.rcsp.history.dto.MessageHistoryResponse;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import com.ktds.rcsp.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "이력조회", description = "메시지 발송 이력 조회 API")
public class HistoryController {

   private final HistoryService historyService;

   @PostMapping("/messages/search")
   @Operation(summary = "메시지 이력 검색", description = "메시지 발송 이력을 검색합니다")
   public ApiResponse<PageResponse<MessageHistoryResponse>> searchMessages(
           @RequestBody MessageHistorySearchRequest request) {
       return ApiResponse.success(historyService.searchMessages(request));
   }

   @PostMapping("/messages/{messageId}/status")
   @Operation(summary = "메시지 상태 업데이트", description = "메시지 발송 상태를 업데이트합니다")
   public ApiResponse<Void> updateMessageStatus(
           @PathVariable String messageId,
           @RequestParam String status,
           @RequestParam(required = false) String resultCode,
           @RequestParam(required = false) String resultMessage) {
       historyService.updateMessageStatus(messageId, status, resultCode, resultMessage);
       return ApiResponse.success(null);
   }
}
