package com.ktds.rcsp.basedata.repository;

import com.ktds.rcsp.basedata.domain.Chatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatbotRepository extends JpaRepository<Chatbot, String> {
   List<Chatbot> findByBrandIdAndStatus(String brandId, String status);
}
