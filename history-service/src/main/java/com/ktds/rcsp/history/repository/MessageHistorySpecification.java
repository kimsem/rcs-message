package com.ktds.rcsp.history.repository;

import com.ktds.rcsp.history.domain.MessageHistory;
import com.ktds.rcsp.history.domain.MessageStatus;
import com.ktds.rcsp.history.dto.MessageHistorySearchRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class MessageHistorySpecification {

   public static Specification<MessageHistory> searchMessageHistory(MessageHistorySearchRequest request) {
       return (root, query, criteriaBuilder) -> {
           List<Predicate> predicates = new ArrayList<>();

           if (request.getStartDate() != null && request.getEndDate() != null) {
               predicates.add(criteriaBuilder.between(root.get("createdAt"), 
                   request.getStartDate(), request.getEndDate()));
           }

           if (request.getBrandId() != null) {
               predicates.add(criteriaBuilder.equal(root.get("brandId"), request.getBrandId()));
           }

           if (request.getChatbotId() != null) {
               predicates.add(criteriaBuilder.equal(root.get("chatbotId"), request.getChatbotId()));
           }

           if (request.getMessageGroupId() != null) {
               predicates.add(criteriaBuilder.equal(root.get("messageGroupId"), 
                   request.getMessageGroupId()));
           }

           if (request.getStatus() != null) {
               predicates.add(criteriaBuilder.equal(root.get("status"), 
                   MessageStatus.valueOf(request.getStatus())));
           }

           return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
       };
   }
}

