package com.ktds.rcsp.history.repository;

import com.ktds.rcsp.history.domain.MessageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HistoryRepository extends JpaRepository<MessageHistory, String>, 
                                       JpaSpecificationExecutor<MessageHistory> {
}
