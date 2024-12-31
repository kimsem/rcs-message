package com.ktds.rcsp.message.repository;

import com.ktds.rcsp.message.domain.MessageGroupSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface MessageGroupSummaryRepository extends JpaRepository<MessageGroupSummary, String> {
}