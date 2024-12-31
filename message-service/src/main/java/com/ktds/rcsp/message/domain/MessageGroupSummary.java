package com.ktds.rcsp.message.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "total_count")  // 실제 테이블의 컬럼명 확인 필요
    private int totalCount;
}