package com.ktds.rcsp.message.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageGroup {
    @Id
    @Column(name = "message_group_id")
    private String messageGroupId;

    @Column(name = "master_id")
    private String masterId;

    @Column(name = "brand_id")
    private String brandId;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "chatbot_id")
    private String chatbotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageGroupStatus status;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Column(name = "created_at")
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
}
