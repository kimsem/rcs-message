package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
   long countByMessageGroup_MessageGroupId(String messageGroupId);

    @Query("SELECT r FROM Recipient r WHERE r.messageGroup.messageGroupId = :messageGroupId")
    List<Recipient> findByMessageGroupId(String messageGroupId);

    @Query("SELECT r.status, COUNT(r) FROM Recipient r WHERE r.messageGroup.messageGroupId = :messageGroupId GROUP BY r.status")
    List<Object[]> countByMessageGroup_MessageGroupIdAndStatusGroup(String messageGroupId);
}
