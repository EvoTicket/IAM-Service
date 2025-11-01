package com.capstone.iamservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}