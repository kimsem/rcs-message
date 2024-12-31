package com.ktds.rcsp.message.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "message_group_id", nullable = false)
    private String messageGroupId;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(nullable = false, length = 4000)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateStatus(MessageStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
