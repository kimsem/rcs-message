package com.ktds.rcsp.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "수신자 정보 업로드 요청")
public class RecipientUploadRequest {

   @Schema(description = "메시지그룹ID")
   private String messageGroupId;

   @NotBlank
   private String brandId;
   @NotBlank
   private String templateId;
   @NotBlank
   private String chatbotId;
   private MultipartFile file;}
