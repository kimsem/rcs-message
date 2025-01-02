package com.ktds.rcsp.message.domain;

public enum MessageGroupStatus {
    CREATED("그룹 생성됨"),
    UPLOADING("수신자 업로드 중"),
    READY("발송 준비 완료"),
    SENDING("발송 중"),
    COMPLETED("발송 완료"),
    FAILED("실패");

    private final String description;

    MessageGroupStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean isProcessing() {
        return this == UPLOADING || this == SENDING;
    }

    public boolean canSendMessage() {
        return this == READY;
    }
}