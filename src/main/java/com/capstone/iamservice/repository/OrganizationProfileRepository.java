package com.capstone.iamservice.repository;

import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile, Long> {

    Optional<OrganizationProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByTaxCode(String taxCode);

    @Query("""
    SELECT o FROM OrganizationProfile o
    WHERE (:status IS NULL OR o.status = :status)
      AND (:provinceCode IS NULL OR o.province.code = :provinceCode)
      AND (
          COALESCE(TRIM(:keyword), '') = '' OR
          LOWER(o.organizationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
          LOWER(o.legalName) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    """)
    Page<OrganizationProfile> advancedSearch(
            @Param("status") OrganizationStatus status,
            @Param("provinceCode") Integer provinceCode,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByStatus(OrganizationStatus status);
}