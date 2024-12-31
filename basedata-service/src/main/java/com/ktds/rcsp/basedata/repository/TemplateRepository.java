package com.ktds.rcsp.basedata.repository;

import com.ktds.rcsp.basedata.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, String> {
   List<Template> findByBrandIdAndStatus(String brandId, String status);
}
