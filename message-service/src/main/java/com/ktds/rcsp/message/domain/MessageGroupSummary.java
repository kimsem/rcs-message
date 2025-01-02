package com.ktds.rcsp.message.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_groups")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageGroupSummary {
    @Id
    @Column(name = "message_group_id")  // 컬럼명도 확인 필요
    private String messageGroupId;

    @Column(name = "master_id", nullable = false)
    private String masterId;  // 추가

    @Column(name = "brand_id", nullable = false)
    private String brandId;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "chatbot_id", nullable = false)
    private String chatbotId;

    @Column(nullable = false)
    private String status;

    @Column(name = "total_count")  // 실제 테이블의 컬럼명 확인 필요
    private int totalCount;

    @Column(name = "processed_count")
    private int processedCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;  // updatedAt도 초기화
        if (this.status == null) {
            this.status = "CREATED";
        }
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // updatedAt과 status를 위한 setter 메서드 추가
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // processed_count를 위한 setter 추가
    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    // total_count를 위한 setter 추가
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }


    public void updateProgress(int processedCount, int totalCount) {
        this.processedCount = processedCount;
        this.totalCount = totalCount;

        if (processedCount == 0) {
            this.status = "UPLOADING";
        } else if (processedCount == totalCount) {
            this.status = "COMPLETED";
        } else {
            this.status = "PROCESSING";
        }
        this.updatedAt = LocalDateTime.now();
    }
}