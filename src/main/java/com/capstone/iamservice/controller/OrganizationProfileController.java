package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.CreateOrganizationRequest;
import com.capstone.iamservice.dto.response.OrganizationCreationResponse;
import com.capstone.iamservice.dto.response.OrganizationProfileResponse;
import com.capstone.iamservice.dto.response.OrganizerAccountProfileResponse;
import com.capstone.iamservice.dto.request.UpdateOrganizationRequest;
import com.capstone.iamservice.dto.request.VerifyOrganizationRequest;
import com.capstone.iamservice.dto.request.AddBankInfoRequest;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.security.JwtUtil;
import com.capstone.iamservice.service.OrganizationProfileService;
import com.capstone.iamservice.security.TokenMetaData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationProfileController {

    private final OrganizationProfileService organizationService;
    private final JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponse<OrganizationCreationResponse>> createOrganization(
            @Valid
            @RequestPart("organization")
            @Parameter(
                    description = "Organization JSON",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateOrganizationRequest.class)
                    )
            )
            CreateOrganizationRequest request,

            @RequestPart(value = "logoFile", required = false)
            @Parameter(description = "Logo image", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
            MultipartFile logoFile,

            @RequestPart(value = "licenseFile", required = false)
            @Parameter(description = "License document/image", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
            MultipartFile licenseFile
    ) {

        OrganizationCreationResponse response = organizationService.createOrganization(request, logoFile, licenseFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created("tạo org profile thành công", response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationProfileResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getMyOrganization() {
        Long userId = jwtUtil.getDataFromAuth().userId();
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @GetMapping("/me/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizerAccountProfileResponse>> getMyAccountProfile() {
        Long userId = jwtUtil.getDataFromAuth().userId();
        OrganizerAccountProfileResponse response = organizationService.getOrganizerAccountProfile(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy chi tiết hồ sơ tài khoản thành công", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationByUserId(@PathVariable Long userId) {
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @PutMapping("/me/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> updateOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request) {

        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn chưa có hồ sơ doanh nghiệp");
        }

        OrganizationProfileResponse response = organizationService.updateOrganization(tokenMetaData.organizationId(), request);
        return ResponseEntity.ok(BaseResponse.ok("Update profile thành công", response));
    }

    @PostMapping(value = "/logo-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload logoUrl")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> uploadUserAvatar(
            @Parameter(
                    description = "File ảnh logo",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file
    ) {
        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn chưa có hồ sơ doanh nghiệp");
        }

        OrganizationProfileResponse org = organizationService.uploadLogoUrl(file, tokenMetaData.userId(), tokenMetaData.organizationId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy thông tin người dùng thành công", org));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(BaseResponse.noContent("Xóa profile thành công"));
    }

    @PostMapping("/bank-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> addBankInfo(
            @Valid @RequestBody AddBankInfoRequest request) {

        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thao tác trên hồ sơ này");
        }

        OrganizationProfileResponse response = organizationService.addBankInfo(organizerId, request);
        return ResponseEntity.ok(BaseResponse.ok("Thêm tài khoản ngân hàng thành công", response));
    }

    @DeleteMapping("/bank-info/{bankInfoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> deleteBankInfo(
            @PathVariable Long bankInfoId) {

        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thao tác trên hồ sơ này");
        }

        OrganizationProfileResponse response = organizationService.deleteBankInfo(organizerId, bankInfoId);
        return ResponseEntity.ok(BaseResponse.ok("Xóa tài khoản ngân hàng thành công", response));
    }

    @GetMapping("/bank-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<OrganizationProfileResponse.BankInfoResponse>>> getBankInfos() {
        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thao tác trên hồ sơ này");
        }

        List<OrganizationProfileResponse.BankInfoResponse> response = organizationService.getBankInfos(organizerId);
        return ResponseEntity.ok(BaseResponse.ok("Xóa tài khoản ngân hàng thành công", response));
    }


    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> verifyOrganization(
            @PathVariable Long id,
            @Valid @RequestBody VerifyOrganizationRequest request
            ) {
        OrganizationProfileResponse response = organizationService.verifyOrganization(id, request);
        return ResponseEntity.ok(BaseResponse.ok("verify hồ sơ thành công", response));
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<BasePageResponse<OrganizationProfileResponse>> advancedSearch(
            @RequestParam(required = false) OrganizationStatus status,
            @RequestParam(required = false) Integer provinceCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));

        Page<OrganizationProfileResponse> response = organizationService.advancedSearch(
                status, provinceCode, keyword, pageable
        );
        return ResponseEntity.ok(BasePageResponse.fromPage(response));
    }
}
