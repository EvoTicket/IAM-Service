package com.capstone.iamservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {

    private String organizationName;
    private String description;
    private String businessAddress;
    private Integer wardCode;
    private Integer provinceCode;
    private String businessPhone;
    private String businessEmail;
    private String website;
    private String businessLicenseUrl;
    private String taxRegistrationUrl;
    private String additionalDocumentsUrl;
}