package com.capstone.iamservice.repository;

import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByCreatedAtAfter(java.time.LocalDateTime date);
    long countByStatus(com.capstone.iamservice.enums.UserStatus status);

    @org.springframework.data.jpa.repository.Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN u.organizationProfile op
    JOIN u.roles r
    WHERE (:roleName IS NULL OR str(r) = :roleName)
      AND (:userStatus IS NULL OR u.status = :userStatus)
      AND (:orgStatus IS NULL OR op.status = :orgStatus)
      AND (:keyword IS NULL OR (
          LOWER(u.email) LIKE :keyword OR
          LOWER(u.firstName) LIKE :keyword OR
          LOWER(u.lastName) LIKE :keyword OR
          LOWER(op.organizationName) LIKE :keyword
      ))
      AND (u.createdAt >= :since)
    """)
    org.springframework.data.domain.Page<User> accountSearch(
            @org.springframework.data.repository.query.Param("roleName") String roleName,
            @org.springframework.data.repository.query.Param("userStatus") com.capstone.iamservice.enums.UserStatus userStatus,
            @org.springframework.data.repository.query.Param("orgStatus") com.capstone.iamservice.enums.OrganizationStatus orgStatus,
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("""
    SELECT u
    FROM User u
    WHERE :role MEMBER OF u.roles
    """)
    Page<User> findByRole(@Param("role") RoleEnum role, Pageable pageable);
}