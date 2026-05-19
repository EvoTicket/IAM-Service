package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.AddressInfo;
import com.capstone.iamservice.dto.request.*;
import com.capstone.iamservice.dto.response.OrganizationCreationResponse;
import com.capstone.iamservice.dto.response.OrganizationProfileResponse;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Role;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.entity.BankInfo;
import com.capstone.iamservice.repository.BankInfoRepository;
import com.capstone.iamservice.repository.OrganizationProfileRepository;
import com.capstone.iamservice.repository.RoleRepository;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.security.JwtService;
import com.capstone.iamservice.security.JwtUtil;
import com.capstone.iamservice.util.LocationUtil;
import com.capstone.iamservice.util.OrganizationUtil;
import com.capstone.iamservice.util.UserUtil;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationProfileService {

    private final OrganizationProfileRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserUtil userUtil;
    private final JwtService jwtService;
    private final JwtUtil jwtUtil;
    private final Cloudinary cloudinary;
    private final OrganizationUtil organizationUtil;
    private final LocationUtil locationUtil;
    private final RoleRepository roleRepository;
    private final BankInfoRepository bankInfoRepository;

    @Value("${app.default.orgAvatarUrl}")
    String orgAvatarUrl;

    @Transactional
    public OrganizationCreationResponse createOrganization(CreateOrganizationRequest request) {
        Long userId = jwtUtil.getDataFromAuth().userId();
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

        final OrganizationProfile finalOrg = organization;
        java.util.List<BankInfo> bankInfoList = request.getBankInfos().stream()
                .map(bi -> BankInfo.builder()
                        .profileName(bi.getProfileName())
                        .organizationProfile(finalOrg)
                        .bankCode(bi.getBankCode())
                        .bankName(bi.getBankName())
                        .bankAccountNumber(bi.getBankAccountNumber())
                        .bankOwnerName(bi.getBankOwnerName())
                        .build())
                .toList();

        organization.setBankInfos(bankInfoList);

        organization = organizationRepository.saveAndFlush(organization);

        Role organizerRole = roleRepository.findByName("ORGANIZER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ORGANIZER")
                        .description("Organizer role")
                        .build()));

        user.getRoles().add(organizerRole);
        user.setOrganizationProfile(organization);
        userRepository.save(user);

        String newToken = jwtService.generateToken(user);
        return OrganizationCreationResponse.builder()
                .newToken(newToken)
                .organizationProfile(mapToResponse(organization))
                .build();
    }

    public OrganizationProfileResponse getOrganizationById(Long id) {
        OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(id);

        return mapToResponse(organization);
    }

    public OrganizationProfileResponse getOrganizationByUserId(Long userId) {
        OrganizationProfile organization = organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Organization profile not found for this user"));

        return mapToResponse(organization);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public OrganizationProfileResponse uploadLogoUrl(MultipartFile file, Long userId, Long organizationId) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh");
        }

        String folder = "logoUrl/" + userId + "/" + organizationId + "/";

        String publicId = UUID.randomUUID().toString();

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", "image");
        options.put("folder",  folder);
        options.put("public_id", publicId);
        options.put("overwrite", true);

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(organizationId);
            organization.setLogoUrl(uploadResult.get("url").toString());
            return mapToResponse(organization);
        } catch (IOException e) {
            throw new AppException(ErrorCode.IO_EXCEPTION, "Không thể tải ảnh lên Cloudinary: " + e.getMessage());
        }
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
            organization.setVerifiedAt(LocalDateTime.now(ZoneOffset.ofHours(7)));
        }

        organization = organizationRepository.save(organization);

        return mapToResponse(organization);
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

        java.util.List<OrganizationProfileResponse.BankInfoResponse> bankInfoResponses = java.util.Collections.emptyList();
        if (org.getBankInfos() != null) {
            bankInfoResponses = org.getBankInfos().stream()
                    .map(bi -> OrganizationProfileResponse.BankInfoResponse.builder()
                            .id(bi.getId())
                            .profileName(bi.getProfileName())
                            .bankCode(bi.getBankCode())
                            .bankName(bi.getBankName())
                            .bankAccountNumber(bi.getBankAccountNumber())
                            .bankOwnerName(bi.getBankOwnerName())
                            .build())
                    .toList();
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
                .bankInfos(bankInfoResponses)
                .build();
    }

    @Transactional
    public OrganizationProfileResponse addBankInfo(Long orgId, AddBankInfoRequest request) {
        OrganizationProfile org = organizationUtil.getOrgProfileOrElseThrow(orgId);

        BankInfo bankInfo = BankInfo.builder()
                .profileName(request.getProfileName())
                .organizationProfile(org)
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankOwnerName(request.getBankOwnerName())
                .build();

        org.getBankInfos().add(bankInfo);
        organizationRepository.save(org);

        return mapToResponse(org);
    }

    @Transactional
    public OrganizationProfileResponse deleteBankInfo(Long orgId, Long bankInfoId) {
        OrganizationProfile org = organizationUtil.getOrgProfileOrElseThrow(orgId);

        long bankCount = org.getBankInfos().size();
        if (bankCount <= 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể xóa tài khoản ngân hàng cuối cùng. Yêu cầu tối thiểu 1 tài khoản.");
        }

        boolean removed = org.getBankInfos().removeIf(bi -> bi.getId().equals(bankInfoId));
        if (!removed) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản ngân hàng cần xóa.");
        }

        organizationRepository.save(org);
        return mapToResponse(org);
    }

    @Transactional
    public List<OrganizationProfileResponse.BankInfoResponse> getBankInfos(Long orgId) {
        OrganizationProfile org = organizationUtil.getOrgProfileOrElseThrow(orgId);

        return org.getBankInfos().stream()
                .map(bi -> OrganizationProfileResponse.BankInfoResponse.builder()
                        .id(bi.getId())
                        .profileName(bi.getProfileName())
                        .bankCode(bi.getBankCode())
                        .bankName(bi.getBankName())
                        .bankAccountNumber(bi.getBankAccountNumber())
                        .bankOwnerName(bi.getBankOwnerName())
                        .build())
                .toList();
    }
}