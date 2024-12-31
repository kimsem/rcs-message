package com.ktds.rcsp.common.event;

/**
 * RCS 메시지 이벤트 구독을 위한 인터페이스
 */
public interface MessageEventSubscriber {
    void subscribeToUploadEvents();
    void subscribeToSendEvents();
    void subscribeToResultEvents();
}