package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
   long countByMessageGroupId(String messageGroupId);
   long countByMessageGroupIdAndStatus(String messageGroupId, String status);
}
