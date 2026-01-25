package com.capstone.iamservice.repository;

import com.capstone.iamservice.entity.OtpToken;
import com.capstone.iamservice.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpType type);

    Optional<OtpToken> findByEmailAndIdAndType(String email, Long id, OtpType type);
}