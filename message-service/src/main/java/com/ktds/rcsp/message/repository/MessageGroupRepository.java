package com.ktds.rcsp.message.repository;


import com.ktds.rcsp.message.domain.MessageGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MessageGroupRepository extends JpaRepository<MessageGroup, String> {
    MessageGroup findByMessageGroupId(String messageGroupId);
    @Query("SELECT m.totalCount FROM MessageGroup m WHERE m.messageGroupId = :messageGroupId")
    Integer getTotalCountByMessageGroupId(String messageGroupId);
    boolean existsByMessageGroupId(String messageGroupId);
}
