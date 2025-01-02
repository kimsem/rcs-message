package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {
}