package com.ktds.rcsp.history.repository;

import com.ktds.rcsp.history.domain.MessageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface HistoryRepository extends MongoRepository<MessageHistory, String> {

//    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
//    Page<MessageHistory> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
//
//    @Query("{ 'brandId': ?0 }")
//    Page<MessageHistory> findByBrandId(String brandId, Pageable pageable);
//
//    @Query("{ 'chatbotId': ?0 }")
//    Page<MessageHistory> findByChatbotId(String chatbotId, Pageable pageable);
//
//    @Query("{ 'messageGroupId': ?0 }")
//    Page<MessageHistory> findByMessageGroupId(String messageGroupId, Pageable pageable);
//
//    @Query("{ 'masterId': ?0 }")
//    Page<MessageHistory> findByMasterId(String masterId, Pageable pageable);
//
//    @Query("{ 'status': ?0 }")
//    Page<MessageHistory> findByStatus(String status, Pageable pageable);
//
//    @Query("{ $and: [ " +
//            "{ 'createdAt': { $gte: ?0, $lte: ?1 } }, " +
//            "{ 'brandId': ?2 }, " +
//            "{ 'chatbotId': ?3 }, " +
//            "{ 'messageGroupId': ?4 }, " +
//            "{ 'masterId': ?5 }, " +
//            "{ 'status': ?6 } " +
//            "] }")
//    Page<MessageHistory> findBySearchCriteria(
//            LocalDateTime startDate,
//            LocalDateTime endDate,
//            String brandId,
//            String chatbotId,
//            String messageGroupId,
//            String masterId,
//            String status,
//            Pageable pageable
//    );

    /*
    *   기존의 개별 메서드들을 하나의 통합 메서드로 대체
    *   $or 연산자를 사용하여 각 조건이 null일 경우 무시되도록 처리
    *   MongoDB의 필드명 규칙에 맞게 수정 (camelCase를 snake_case로)
    * */
    @Query(value = "{" +
            "'master_id': ?0, " +
            "'created_at': { $gte: ?1, $lte: ?2 }, " +
            "$and: [" +
            "  { $or: [ " +
            "    { $and: [ " +
            "      { $exists: { $eq: [?3, true] } }, " +
            "      { 'brand_id': ?3 } " +
            "    ]}, " +
            "    { $exists: { $eq: [?3, false] } } " +
            "  ]}, " +
            "  { $or: [ " +
            "    { $and: [ " +
            "      { $exists: { $eq: [?4, true] } }, " +
            "      { 'chatbot_id': ?4 } " +
            "    ]}, " +
            "    { $exists: { $eq: [?4, false] } } " +
            "  ]}, " +
            "  { $or: [ " +
            "    { $and: [ " +
            "      { $exists: { $eq: [?5, true] } }, " +
            "      { 'message_group_id': ?5 } " +
            "    ]}, " +
            "    { $exists: { $eq: [?5, false] } } " +
            "  ]}, " +
            "  { $or: [ " +
            "    { $and: [ " +
            "      { $exists: { $eq: [?6, true] } }, " +
            "      { 'status': ?6 } " +
            "    ]}, " +
            "    { $exists: { $eq: [?6, false] } } " +
            "  ]} " +
            "]}",
            sort = "{ 'created_at': -1 }")  // -1은 DESC를 의미
    Page<MessageHistory> findBySearchCriteria(
            String masterId,          // 필수
            LocalDateTime startDate,  // 필수
            LocalDateTime endDate,    // 필수
            String brandId,           // 선택
            String chatbotId,         // 선택
            String messageGroupId,    // 선택
            String status,            // 선택
            Pageable pageable);
}