package com.ktds.rcsp.message.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class RecipientResponse {
    private final Long recipientId;
    private final String phoneNumber;
    private final String status;
    private final LocalDateTime createdAt;
}