package com.ktds.rcsp.basedata.service;

import com.ktds.rcsp.basedata.dto.BrandResponse;
import com.ktds.rcsp.basedata.dto.TemplateResponse;
import com.ktds.rcsp.basedata.dto.ChatbotResponse;
import java.util.List;

public interface BaseDataService {
   List<BrandResponse> getBrands(String masterId);
   List<TemplateResponse> getTemplates(String brandId);
   List<ChatbotResponse> getChatbots(String brandId);
}
