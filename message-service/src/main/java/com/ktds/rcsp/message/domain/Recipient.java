package com.ktds.rcsp.message.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "recipients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recipient {

    @Id
    @Column(name = "recipient_id")
    private Long id;

    @Column(name = "message_group_id", nullable = false)
    private String messageGroupId;

    @Column(name = "encrypted_phone", nullable = false)
    private String encryptedPhone;  // encryptedPhoneNumber에서 변경

//    @Column(name = "encrypted_name", nullable = false)
//    private String encryptedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.id = generateId();
        this.createdAt = LocalDateTime.now();
    }

    private Long generateId() {
        return Math.abs(new Random().nextLong()) % 100000000000000L; // 14자리 양수
    }


    @Builder
//    public Recipient(String messageGroupId, String encryptedPhone, String encryptedName, ProcessingStatus status) {
    public Recipient(String messageGroupId, String encryptedPhone, ProcessingStatus status) {

            this.messageGroupId = messageGroupId;
        this.encryptedPhone = encryptedPhone;
//        this.encryptedName = encryptedName;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
