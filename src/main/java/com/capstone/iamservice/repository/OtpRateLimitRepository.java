package com.capstone.iamservice.repository;

import com.capstone.iamservice.entity.OtpRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRateLimitRepository extends JpaRepository<OtpRateLimit, Long> {
    Optional<OtpRateLimit> findByEmail(String email);
}
