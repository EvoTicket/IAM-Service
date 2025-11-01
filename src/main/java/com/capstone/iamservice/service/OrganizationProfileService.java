package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.request.*;
import com.capstone.iamservice.dto.response.OrganizationProfileResponse;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.OrganizationProfileRepository;
import com.capstone.iamservice.util.LocationUtil;
import com.capstone.iamservice.util.OrganizationUtil;
import com.capstone.iamservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationProfileService {

    private final OrganizationProfileRepository organizationRepository;
    private final OrganizationUtil organizationUtil;
    private final LocationUtil locationUtil;
    private final UserUtil userUtil;

    @Value("${app.default.orgAvatarUrl}")
    String orgAvatarUrl;

    @Transactional
    public OrganizationProfileResponse createOrganization(Long userId, CreateOrganizationRequest request) {
        User user = userUtil.getUserOrThrow(userId);

        if (organizationRepository.existsByUserId(userId)) {
            throw new AppException(ErrorCode.CONFLICT, "User already has an organization profile");
        }

        if (organizationRepository.existsByTaxCode(request.getTaxCode())) {
            throw new AppException(ErrorCode.CONFLICT, "Tax code already exists");
        }

        Ward ward = null;
        Province province = null;

        if (request.getWardCode() != null) {
            ward = locationUtil.getWardByCode(request.getWardCode());
        }

        if (request.getProvinceCode() != null) {
            province = locationUtil.getProvinceByCode(request.getProvinceCode());
        }

        OrganizationProfile organization = OrganizationProfile.builder()
                .user(user)
                .organizationName(request.getOrganizationName())
                .legalName(request.getLegalName())
                .taxCode(request.getTaxCode())
                .logoUrl(orgAvatarUrl)
                .description(request.getDescription())
                .businessAddress(request.getBusinessAddress())
                .ward(ward)
                .province(province)
                .businessPhone(request.getBusinessPhone())
                .businessEmail(request.getBusinessEmail())
                .website(request.getWebsite())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .status(OrganizationStatus.PENDING)
                .build();

        organization = organizationRepository.save(organization);

        return mapToResponse(organization);
    }

    public OrganizationProfileResponse getOrganizationById(Long id) {
        OrganizationProfile organization = organizationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Organization not found"));

        return mapToResponse(organization);
    }

    public OrganizationProfileResponse getOrganizationByUserId(Long userId) {
        OrganizationProfile organization = organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Organization profile not found for this user"));

        return mapToResponse(organization);
    }

    @Transactional
    public OrganizationProfileResponse updateOrganization(Long id, UpdateOrganizationRequest request) {
        OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(id);

        if (request.getOrganizationName() != null) {
            organization.setOrganizationName(request.getOrganizationName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getBusinessAddress() != null) {
            organization.setBusinessAddress(request.getBusinessAddress());
        }
        if (request.getBusinessPhone() != null) {
            organization.setBusinessPhone(request.getBusinessPhone());
        }
        if (request.getBusinessEmail() != null) {
            organization.setBusinessEmail(request.getBusinessEmail());
        }
        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }
        if (request.getBusinessLicenseUrl() != null) {
            organization.setBusinessLicenseUrl(request.getBusinessLicenseUrl());
        }

        if (request.getWardCode() != null) {
            Ward ward = locationUtil.getWardByCode(request.getWardCode());
            organization.setWard(ward);
        }

        if (request.getProvinceCode() != null) {
            Province province = locationUtil.getProvinceByCode(request.getProvinceCode());
            organization.setProvince(province);
        }

        organization = organizationRepository.save(organization);

        return mapToResponse(organization);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(id);

        organizationRepository.delete(organization);
    }

    @Transactional
    public OrganizationProfileResponse verifyOrganization(Long id, VerifyOrganizationRequest request) {
        OrganizationProfile organization =organizationUtil.getOrgProfileOrElseThrow(id);

        if (request.getStatus() == OrganizationStatus.REJECTED &&
                (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Rejection reason is required when rejecting");
        }

        organization.setStatus(request.getStatus());
        organization.setRejectionReason(request.getRejectionReason());

        if (request.getStatus() == OrganizationStatus.VERIFIED) {
            organization.setVerifiedAt(OffsetDateTime.now(ZoneOffset.ofHours(7)));
        }

        organization = organizationRepository.save(organization);

        return mapToResponse(organization);
    }

    public Page<OrganizationProfileResponse> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }


    public Page<OrganizationProfileResponse> advancedSearch(
            OrganizationStatus status,
            Integer provinceCode,
            String keyword,
            Pageable pageable) {
        return organizationRepository.advancedSearch(status, provinceCode, keyword, pageable)
                .map(this::mapToResponse);
    }

    private OrganizationProfileResponse mapToResponse(OrganizationProfile org) {
        AddressInfo addressInfo = null;

        if (org.getWard() != null || org.getProvince() != null) {
            addressInfo = locationUtil.getAddressInfo(org.getProvince(), org.getWard(), org.getFullAddress());
        }

        return OrganizationProfileResponse.builder()
                .id(org.getId())
                .userId(org.getUser().getId())
                .userEmail(org.getUser().getEmail())
                .organizationName(org.getOrganizationName())
                .legalName(org.getLegalName())
                .taxCode(org.getTaxCode())
                .logoUrl(org.getLogoUrl())
                .description(org.getDescription())
                .businessAddress(org.getBusinessAddress())
                .addressInfo(addressInfo)
                .businessPhone(org.getBusinessPhone())
                .businessEmail(org.getBusinessEmail())
                .website(org.getWebsite())
                .businessLicenseUrl(org.getBusinessLicenseUrl())
                .status(org.getStatus())
                .rejectionReason(org.getRejectionReason())
                .verifiedAt(org.getVerifiedAt())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}