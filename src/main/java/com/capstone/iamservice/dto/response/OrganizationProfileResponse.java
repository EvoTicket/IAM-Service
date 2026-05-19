package com.capstone.iamservice.dto.response;

import com.capstone.iamservice.enums.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationProfileResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String organizationName;
    private String legalName;
    private String taxCode;
    private String logoUrl;
    private String description;
    private String businessAddress;
    private AddressInfo addressInfo;
    private String businessPhone;
    private String businessEmail;
    private String website;
    private String businessLicenseUrl;
    private OrganizationStatus status;
    private String rejectionReason;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BankInfoResponse> bankInfos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankInfoResponse {
        private Long id;
        private String profileName;
        private String bankCode;
        private String bankName;
        private String bankAccountNumber;
        private String bankOwnerName;
    }
}