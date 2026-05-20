package com.capstone.iamservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String organizationName;

    @NotBlank(message = "Legal name is required")
    @Size(max = 500, message = "Legal name must not exceed 500 characters")
    private String legalName;

    @NotBlank(message = "Tax code is required")
    @Pattern(regexp = "^\\d{10}(-\\d{3})?$", message = "Invalid tax code format")
    private String taxCode;

    private String description;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    private Integer wardCode;
    private Integer provinceCode;

    @Pattern(regexp = "^\\d{10,11}$", message = "Invalid phone number")
    private String businessPhone;

    @Email(message = "Invalid email format")
    private String businessEmail;

    private String website;

    private String businessLicenseUrl;

    private String logoUrl;
    private String shortDescription;
    private String publicBio;
    private String businessType;
    private String billingAddress;
    private String organizationType;
    private String verificationLevel;

    @NotEmpty(message = "At least one bank account is required")
    private List<BankInfoRequest> bankInfos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankInfoRequest {
        @NotBlank(message = "Profile name is required")
        private String profileName;

        @NotBlank(message = "Bank code is required")
        private String bankCode;

        @NotBlank(message = "Bank name is required")
        private String bankName;

        @NotBlank(message = "Bank account number is required")
        private String bankAccountNumber;

        @NotBlank(message = "Bank owner name is required")
        private String bankOwnerName;
    }
}