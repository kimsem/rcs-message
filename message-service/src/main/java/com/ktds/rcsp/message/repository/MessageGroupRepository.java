package com.ktds.rcsp.message.repository;


import com.ktds.rcsp.message.domain.MessageGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageGroupRepository extends JpaRepository<MessageGroup, String> {
    MessageGroup findByMessageGroupId(String messageGroupId);

    boolean existsByMessageGroupId(String messageGroupId);
}
