package com.ktds.rcsp.basedata.repository;

import com.ktds.rcsp.basedata.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, String> {
   List<Brand> findByMasterIdAndStatus(String masterId, String status);
}
