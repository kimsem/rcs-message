package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.Recipient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
   long countByMessageGroupId(String messageGroupId);
   long countByMessageGroupIdAndStatus(String messageGroupId, String status);

    List<Recipient> findByMessageGroupId(String messageGroupId);
}
