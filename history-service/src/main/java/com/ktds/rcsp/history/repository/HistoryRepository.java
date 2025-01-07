package com.ktds.rcsp.history.repository;

import com.ktds.rcsp.history.domain.MessageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface HistoryRepository extends MongoRepository<MessageHistory, String> {

    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    Page<MessageHistory> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("{ 'brandId': ?0 }")
    Page<MessageHistory> findByBrandId(String brandId, Pageable pageable);

    @Query("{ 'chatbotId': ?0 }")
    Page<MessageHistory> findByChatbotId(String chatbotId, Pageable pageable);

    @Query("{ 'messageGroupId': ?0 }")
    Page<MessageHistory> findByMessageGroupId(String messageGroupId, Pageable pageable);

    @Query("{ 'masterId': ?0 }")
    Page<MessageHistory> findByMasterId(String masterId, Pageable pageable);

    @Query("{ 'status': ?0 }")
    Page<MessageHistory> findByStatus(String status, Pageable pageable);

    @Query("{ $and: [ " +
            "{ 'createdAt': { $gte: ?0, $lte: ?1 } }, " +
            "{ 'brandId': ?2 }, " +
            "{ 'chatbotId': ?3 }, " +
            "{ 'messageGroupId': ?4 }, " +
            "{ 'status': ?6 } " +
            "] }")
    Page<MessageHistory> findBySearchCriteria(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String brandId,
            String chatbotId,
            String messageGroupId,
            String masterId,
            String status,
            Pageable pageable
    );

}