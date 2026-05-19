package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.AccountDetailsResponse;
import com.capstone.iamservice.dto.response.AccountSummaryResponse;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.repository.OrganizationProfileRepository;
import com.capstone.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final OrganizationProfileRepository organizationProfileRepository;

    public AccountSummaryResponse getAccountSummary() {
        return AccountSummaryResponse.builder()
                .totalAccounts(userRepository.count())
                .activeOrganizers(organizationProfileRepository.countByStatus(OrganizationStatus.VERIFIED))
                .pendingApprovals(organizationProfileRepository.countByStatus(OrganizationStatus.PENDING))
                .restrictedAccounts(userRepository.countByStatus(UserStatus.BANNED))
                .build();
    }

    public Page<AccountDetailsResponse> searchAccounts(
            String roleName,
            UserStatus userStatus,
            OrganizationStatus orgStatus,
            String keyword,
            Integer days,
            Pageable pageable
    ) {
        // Use a far-past date instead of null to avoid PostgreSQL parameter type inference errors
        LocalDateTime since = (days != null) ? LocalDateTime.now().minusDays(days) : LocalDateTime.of(1970, 1, 1, 0, 0);
        String keywordPattern = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.toLowerCase() + "%" : null;
        
        return userRepository.accountSearch(roleName, userStatus, orgStatus, keywordPattern, since, pageable)
                .map(this::mapToAccountDetails);
    }

    private AccountDetailsResponse mapToAccountDetails(User user) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("BUYER");

        String verificationStatus = "N/A";
        if (user.getOrganizationProfile() != null) {
            verificationStatus = user.getOrganizationProfile().getStatus().name();
        }

        return AccountDetailsResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role)
                .phoneNumber(user.getPhoneNumber())
                .verificationStatus(verificationStatus)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastActive(user.getUpdatedAt()) // Tạm thời dùng updatedAt
                .build();
    }

    public Page<AccountDetailsResponse> getCheckers(Pageable pageable) {
        return userRepository.findAllCheckers(pageable)
                .map(this::mapToAccountDetails);
    }
}
