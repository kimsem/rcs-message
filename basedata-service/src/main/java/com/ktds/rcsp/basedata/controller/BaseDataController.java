package com.ktds.rcsp.basedata.controller;

import com.ktds.rcsp.basedata.dto.BrandResponse;
import com.ktds.rcsp.basedata.dto.TemplateResponse;
import com.ktds.rcsp.basedata.dto.ChatbotResponse;
import com.ktds.rcsp.basedata.service.BaseDataService;
import com.ktds.rcsp.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base")
@RequiredArgsConstructor
@Tag(name = "기준정보", description = "기준정보 조회 API")
@Slf4j
public class BaseDataController {

   private final BaseDataService baseDataService;

   @GetMapping("/brands")
   @Operation(summary = "브랜드 목록 조회", description = "기업의 브랜드 목록을 조회합니다")
   public ApiResponse<List<BrandResponse>> getBrands(@RequestParam String masterId) {
      log.info("Retrieving brands for masterId: {}", masterId);
      List<BrandResponse> brands = baseDataService.getBrands(masterId);
      log.info("Retrieved {} brands for masterId: {}", brands.size(), masterId);
      return ApiResponse.success(brands);
//       return ApiResponse.success(baseDataService.getBrands(masterId));
   }

   @GetMapping("/templates")
   @Operation(summary = "템플릿 목록 조회", description = "브랜드의 템플릿 목록을 조회합니다")
   public ApiResponse<List<TemplateResponse>> getTemplates(@RequestParam String brandId) {
       return ApiResponse.success(baseDataService.getTemplates(brandId));
   }

   @GetMapping("/chatbots")
   @Operation(summary = "발신번호 목록 조회", description = "브랜드의 발신번호 목록을 조회합니다")
   public ApiResponse<List<ChatbotResponse>> getChatbots(@RequestParam String brandId) {
       return ApiResponse.success(baseDataService.getChatbots(brandId));
   }
}
