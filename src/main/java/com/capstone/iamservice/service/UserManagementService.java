package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.AccountDetailResponse;
import com.capstone.iamservice.dto.response.AccountDetailsResponse;
import com.capstone.iamservice.dto.response.AccountSummaryResponse;
import com.capstone.iamservice.entity.BankInfo;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.enums.RoleEnum;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.OrganizationProfileRepository;
import com.capstone.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        LocalDateTime since = (days != null) ? LocalDateTime.now().minusDays(days) : LocalDateTime.now().minusDays(365);
        String keywordPattern = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.toLowerCase() + "%" : null;
        
        return userRepository.accountSearch(roleName, userStatus, orgStatus, keywordPattern, since, pageable)
                .map(this::mapToAccountDetails);
    }

    private AccountDetailsResponse mapToAccountDetails(User user) {
        RoleEnum role = user.getRoles().stream()
                .anyMatch(r -> r == RoleEnum.ORGANIZER)
                ? RoleEnum.ORGANIZER
                : RoleEnum.USER;

        String verificationStatus = "VERIFIED";
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
        return userRepository.findByRole(RoleEnum.CHECKER, pageable)
                .map(this::mapToAccountDetails);
    }

    public AccountDetailResponse getAccountDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy user với ID: " + id));

        boolean isOrg = user.getRoles().contains(RoleEnum.ORGANIZER);
        String type = isOrg ? "Organizer" : "Buyer";

        String status = "Active";
        if (user.getStatus() == UserStatus.BANNED) {
            status = "Suspended";
        } else if (isOrg && user.getOrganizationProfile() != null) {
            OrganizationStatus opStatus = user.getOrganizationProfile().getStatus();
            if (opStatus == OrganizationStatus.PENDING) {
                status = "Pending Approval";
            } else if (opStatus == OrganizationStatus.REJECTED) {
                status = "Restricted";
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String registeredDate = user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "N/A";
        String lastActiveDate = user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : "N/A";

        // Stats
        String verificationStatus = "Verified";
        int docsSubmitted = 0;
        int totalDocs = 0;
        String riskLevel = "Low";
        String riskReason = "N/A";

        if (isOrg) {
            OrganizationProfile op = user.getOrganizationProfile();
            if (op != null) {
                verificationStatus = op.getStatus() == OrganizationStatus.VERIFIED ? "Verified" :
                        op.getStatus() == OrganizationStatus.PENDING ? "Pending Approval" : "Rejected";
                docsSubmitted = (op.getBusinessLicenseUrl() != null && !op.getBusinessLicenseUrl().isEmpty()) ? 1 : 0;
                totalDocs = 1;
            } else {
                verificationStatus = "Pending Approval";
                totalDocs = 1;
            }
        }

        AccountDetailResponse.Stats stats = AccountDetailResponse.Stats.builder()
                .verificationStatus(verificationStatus)
                .documentsSubmitted(docsSubmitted)
                .totalDocuments(totalDocs)
                .eventsCreated(0) // Hardcoded
                .pendingEvents(0) // Hardcoded
                .lastActive(lastActiveDate)
                .riskLevel(riskLevel)
                .riskReason(riskReason)
                .build();

        // Profile
        String representative = user.getFullName();
        String orgType = isOrg ? "Doanh nghiệp tổ chức sự kiện" : "Cá nhân";
        String email = user.getEmail();
        String phone = user.getPhoneNumber();
        String taxId = "N/A";
        String address = user.getFullAddress();

        if (isOrg && user.getOrganizationProfile() != null) {
            OrganizationProfile op = user.getOrganizationProfile();
            if (op.getLegalName() != null && !op.getLegalName().isEmpty()) {
                representative = op.getLegalName();
            }
            if (op.getOrganizationType() != null && !op.getOrganizationType().isEmpty()) {
                orgType = op.getOrganizationType();
            }
            if (op.getBusinessEmail() != null && !op.getBusinessEmail().isEmpty()) {
                email = op.getBusinessEmail();
            }
            if (op.getBusinessPhone() != null && !op.getBusinessPhone().isEmpty()) {
                phone = op.getBusinessPhone();
            }
            if (op.getTaxCode() != null && !op.getTaxCode().isEmpty()) {
                taxId = op.getTaxCode();
            }
            address = op.getFullAddress();
        }

        AccountDetailResponse.Profile profile = AccountDetailResponse.Profile.builder()
                .representative(representative)
                .orgType(orgType)
                .email(email)
                .phone(phone)
                .taxId(taxId)
                .address(address)
                .build();

        // Documents
        List<AccountDetailResponse.DocumentDto> documents = new ArrayList<>();
        if (isOrg && user.getOrganizationProfile() != null) {
            OrganizationProfile op = user.getOrganizationProfile();
            if (op.getBusinessLicenseUrl() != null && !op.getBusinessLicenseUrl().isEmpty()) {
                String docStatus = op.getStatus() == OrganizationStatus.VERIFIED ? "Verified" :
                        op.getStatus() == OrganizationStatus.PENDING ? "Pending" : "Rejected";
                documents.add(AccountDetailResponse.DocumentDto.builder()
                        .name("Giấy phép kinh doanh")
                        .status(docStatus)
                        .url(op.getBusinessLicenseUrl())
                        .build());
            }
        }

        // Payout Account
        AccountDetailResponse.PayoutAccount payoutAccount = null;
        if (isOrg && user.getOrganizationProfile() != null && !user.getOrganizationProfile().getBankInfos().isEmpty()) {
            BankInfo bankInfo = user.getOrganizationProfile().getBankInfos().get(0);
            payoutAccount = AccountDetailResponse.PayoutAccount.builder()
                    .bank(bankInfo.getBankName() != null ? bankInfo.getBankName() : bankInfo.getBankCode())
                    .accountNumber(bankInfo.getBankAccountNumber())
                    .accountName(bankInfo.getBankOwnerName())
                    .status("Verified")
                    .build();
        } else if (user.getBankAccountNumber() != null && !user.getBankAccountNumber().isEmpty()) {
            payoutAccount = AccountDetailResponse.PayoutAccount.builder()
                    .bank(user.getBankCode())
                    .accountNumber(user.getBankAccountNumber())
                    .accountName(user.getBankAccountName())
                    .status("Verified")
                    .build();
        }

        // Operational Context
        AccountDetailResponse.OperationalContext operationalContext = AccountDetailResponse.OperationalContext.builder()
                .summary(AccountDetailResponse.Summary.builder()
                        .recentEventsCount(0)
                        .flaggedEventsCount(0)
                        .pendingPayoutCount(0)
                        .openSupportNotesCount(0)
                        .build())
                .recentEvents(Collections.emptyList())
                .adminLogs(Collections.emptyList())
                .activities(Collections.emptyList())
                .history(Collections.emptyList())
                .build();

        // Admin Context
        String internalNote = "";
        if (isOrg && user.getOrganizationProfile() != null) {
            internalNote = user.getOrganizationProfile().getRejectionReason();
        }
        AccountDetailResponse.AdminContext adminContext = AccountDetailResponse.AdminContext.builder()
                .internalNote(internalNote != null ? internalNote : "")
                .lastAction(null)
                .build();

        return AccountDetailResponse.builder()
                .id(user.getId())
                .name(isOrg && user.getOrganizationProfile() != null ? user.getOrganizationProfile().getOrganizationName() : user.getFullName())
                .type(type)
                .status(status)
                .registeredDate(registeredDate)
                .stats(stats)
                .profile(profile)
                .documents(documents)
                .payoutAccount(payoutAccount)
                .operationalContext(operationalContext)
                .adminContext(adminContext)
                .build();
    }
}
