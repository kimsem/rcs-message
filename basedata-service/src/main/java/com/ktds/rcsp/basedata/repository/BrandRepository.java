package com.ktds.rcsp.basedata.repository;

import com.ktds.rcsp.basedata.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {
   @Query("SELECT b FROM Brand b WHERE b.masterId = :masterId AND b.status = :status")
   List<Brand> findByMasterIdAndStatus(
           @Param("masterId") String masterId,
           @Param("status") String status
   );
}
