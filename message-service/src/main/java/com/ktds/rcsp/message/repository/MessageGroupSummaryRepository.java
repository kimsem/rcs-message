package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.MessageGroupSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageGroupSummaryRepository extends JpaRepository<MessageGroupSummary, String> {
}