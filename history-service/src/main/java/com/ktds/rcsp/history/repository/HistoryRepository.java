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
    *   @Param 어노테이션을 사용하여 명확한 파라미터 매핑
    * */
    @Query("{ " +
            "'master_id': :#{#masterId}, " +
            "$and: [ " +
            "  { $or: [ " +
            "    { 'created_at': { $gte: :#{#startDate}, $lte: :#{#endDate} } }, " +
            "    { $and: [ { :#{#startDate} : null }, { :#{#endDate} : null } ] } " +
            "  ] }, " +
            "  { $or: [ " +
            "    { 'brand_id': :#{#brandId} }, " +
            "    { :#{#brandId} : null } " +
            "  ] }, " +
            "  { $or: [ " +
            "    { 'chatbot_id': :#{#chatbotId} }, " +
            "    { :#{#chatbotId} : null } " +
            "  ] }, " +
            "  { $or: [ " +
            "    { 'message_group_id': :#{#messageGroupId} }, " +
            "    { :#{#messageGroupId} : null } " +
            "  ] }, " +
            "  { $or: [ " +
            "    { 'status': :#{#status} }, " +
            "    { :#{#status} : null } " +
            "  ] } " +
            "] }")
    Page<MessageHistory> findBySearchCriteria(
            @Param("masterId") String masterId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("brandId") String brandId,
            @Param("chatbotId") String chatbotId,
            @Param("messageGroupId") String messageGroupId,
            @Param("status") String status,
            Pageable pageable
    );

}