package com.ktds.rcsp.common.event;

/**
 * RCS 메시지 이벤트 발행을 위한 인터페이스
 */
public interface MessageEventPublisher {
    void publishUploadEvent(RecipientUploadEvent event);
    void publishSendEvent(MessageSendEvent event);
    void publishResultEvent(MessageResultEvent event);
}