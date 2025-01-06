package com.ktds.rcsp.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "Unauthorized access"),

    // Auth Errors
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "Invalid credentials"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "Token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Invalid token"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "User not found"),

    // Message Errors
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "Message not found"),
    INVALID_MESSAGE_STATUS(HttpStatus.BAD_REQUEST, "M002", "Invalid message status"),
    RECIPIENT_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M003", "Failed to upload recipients"),
    ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M004", "Failed to encrypt data"),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "M005", "Invalid file format"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "M006", "File size exceeds limit"),
    MESSAGE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "메시지 그룹을 찾을 수 없습니다"),

    // 수신자 관련
    NO_RECIPIENTS(HttpStatus.BAD_REQUEST, "M003", "메시지 그룹에 수신자가 없습니다"),
    INVALID_RECIPIENT_FORMAT(HttpStatus.BAD_REQUEST, "M004", "수신자 정보 형식이 올바르지 않습니다"),
    RECIPIENT_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M005", "수신자 정보 암호화에 실패했습니다"),

    // 파일 관련
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "M006", "파일이 없습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "M008", "파일 크기가 제한을 초과했습니다"),

    // 발송 관련
    SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M009", "메시지 발송에 실패했습니다"),
    EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M011", "이벤트 발행에 실패했습니다"),
    DUPLICATE_MESSAGE_GROUP_ID(HttpStatus.BAD_REQUEST, "M012", "이미 존재하는 메시지 그룹 아이디입니다"),

    // 메시지 처리 관련
    MESSAGE_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M013", "메시지 처리 중 오류가 발생했습니다"),


    // Base Data Errors
    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "Brand not found"),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "B002", "Template not found"),
    CHATBOT_NOT_FOUND(HttpStatus.NOT_FOUND, "B003", "Chatbot not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
