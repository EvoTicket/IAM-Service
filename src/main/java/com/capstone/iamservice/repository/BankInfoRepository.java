package com.capstone.iamservice.repository;

import com.capstone.iamservice.entity.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankInfoRepository extends JpaRepository<BankInfo, Long> {
    List<BankInfo> findByOrganizationProfileId(Long organizationProfileId);
    long countByOrganizationProfileId(Long organizationProfileId);
}
