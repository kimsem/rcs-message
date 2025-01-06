package com.ktds.rcsp.common.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final T data;
    private final ErrorResponse error;  // 에러 정보를 담을 필드 추가
    private final LocalDateTime timestamp;

    private ApiResponse(int status, T data, ErrorResponse error) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, null);
    }

    public static <T> ApiResponse<T> error(int status, ErrorResponse error) {
        return new ApiResponse<>(status, null, error);
    }
}
