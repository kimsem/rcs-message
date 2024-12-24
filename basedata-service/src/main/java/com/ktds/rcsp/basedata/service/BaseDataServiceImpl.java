package com.ktds.rcsp.basedata.service;

import com.ktds.rcsp.basedata.domain.Brand;
import com.ktds.rcsp.basedata.domain.Template;
import com.ktds.rcsp.basedata.domain.Chatbot;
import com.ktds.rcsp.basedata.dto.BrandResponse;
import com.ktds.rcsp.basedata.dto.TemplateResponse;
import com.ktds.rcsp.basedata.dto.ChatbotResponse;
import com.ktds.rcsp.basedata.repository.BrandRepository;
import com.ktds.rcsp.basedata.repository.TemplateRepository;
import com.ktds.rcsp.basedata.repository.ChatbotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseDataServiceImpl implements BaseDataService {

   private final BrandRepository brandRepository;
   private final TemplateRepository templateRepository;
   private final ChatbotRepository chatbotRepository;

   private static final String ACTIVE_STATUS = "ACTIVE";

   @Override
   @Transactional(readOnly = true)
   @Cacheable(value = "brands", key = "#masterId")
   public List<BrandResponse> getBrands(String masterId) {
       return brandRepository.findByMasterIdAndStatus(masterId, ACTIVE_STATUS)
               .stream()
               .map(this::convertToBrandResponse)
               .collect(Collectors.toList());
   }

   @Override
   @Transactional(readOnly = true)
   @Cacheable(value = "templates", key = "#brandId")
   public List<TemplateResponse> getTemplates(String brandId) {
       return templateRepository.findByBrandIdAndStatus(brandId, ACTIVE_STATUS)
               .stream()
               .map(this::convertToTemplateResponse)
               .collect(Collectors.toList());
   }

   @Override
   @Transactional(readOnly = true)
   @Cacheable(value = "chatbots", key = "#brandId")
   public List<ChatbotResponse> getChatbots(String brandId) {
       return chatbotRepository.findByBrandIdAndStatus(brandId, ACTIVE_STATUS)
               .stream()
               .map(this::convertToChatbotResponse)
               .collect(Collectors.toList());
   }

   private BrandResponse convertToBrandResponse(Brand brand) {
       return BrandResponse.builder()
               .brandId(brand.getBrandId())
               .brandName(brand.getBrandName())
               .build();
   }

   private TemplateResponse convertToTemplateResponse(Template template) {
       return TemplateResponse.builder()
               .templateId(template.getTemplateId())
               .templateName(template.getTemplateName())
               .content(template.getContent())
               .build();
   }

   private ChatbotResponse convertToChatbotResponse(Chatbot chatbot) {
       return ChatbotResponse.builder()
               .chatbotId(chatbot.getChatbotId())
               .phoneNumber(chatbot.getPhoneNumber())
               .build();
   }
}
