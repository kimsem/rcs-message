package com.ktds.rcsp.message.dto;

import com.ktds.rcsp.message.domain.Recipient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "수신자 정보 DTO")
public class RecipientResponse {
    @Schema(description = "수신자 ID")
    private final String recipientId;

    @Schema(description = "수신자 전화번호")
    private final String phoneNumber;

    @Schema(description = "상태")
    private final String status;

    @Schema(description = "생성 일시")
    private final LocalDateTime createdAt;

    public static RecipientResponse from(Recipient recipient) {
        return RecipientResponse.builder()
                .recipientId(String.valueOf(recipient.getId()))
                .phoneNumber(recipient.getEncryptedPhone())
                .status(recipient.getStatus().name())
                .createdAt(recipient.getCreatedAt())
                .build();
    }
}
