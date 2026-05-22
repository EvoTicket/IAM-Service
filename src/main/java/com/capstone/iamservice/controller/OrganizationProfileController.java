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
    @Operation(
            summary = "Tạo hồ sơ tổ chức",
            description = "Tạo mới hồ sơ tổ chức (organization profile) cho user hiện tại. Payload `organization` là JSON, các file (logo/license/cover) là tuỳ chọn."
    )
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
            MultipartFile licenseFile,

            @RequestPart(value = "coverFile", required = false)
            @Parameter(description = "Cover image", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
            MultipartFile coverFile
    ) {

        OrganizationCreationResponse response = organizationService.createOrganization(request, logoFile, licenseFile, coverFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created("tạo org profile thành công", response));
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy hồ sơ tổ chức theo ID",
            description = "Trả về thông tin chi tiết hồ sơ tổ chức theo `id`."
    )
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationProfileResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }


    @GetMapping("/me")
    @Operation(
            summary = "Lấy hồ sơ tổ chức của tôi",
            description = "Lấy hồ sơ tổ chức gắn với user đang đăng nhập (lấy `userId` từ JWT)."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getMyOrganization() {
        Long userId = jwtUtil.getDataFromAuth().userId();
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @GetMapping("/me/account")
    @Operation(
            summary = "Lấy hồ sơ tài khoản organizer của tôi",
            description = "Trả về thông tin hồ sơ tài khoản organizer của user đang đăng nhập."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizerAccountProfileResponse>> getMyAccountProfile() {
        Long userId = jwtUtil.getDataFromAuth().userId();
        OrganizerAccountProfileResponse response = organizationService.getOrganizerAccountProfile(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy chi tiết hồ sơ tài khoản thành công", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Lấy hồ sơ tổ chức theo userId",
            description = "Trả về hồ sơ tổ chức gắn với `userId`."
    )
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationByUserId(
            @Parameter(description = "ID của user") @PathVariable Long userId
    ) {
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @PutMapping("/me/update")
    @Operation(
            summary = "Cập nhật hồ sơ tổ chức của tôi",
            description = "Cập nhật thông tin hồ sơ tổ chức của user đang đăng nhập. Chỉ áp dụng cho tài khoản đã có organization profile."
    )
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
    @Operation(
            summary = "Upload logo",
            description = "Upload file ảnh logo và cập nhật logoUrl cho organization hiện tại."
    )
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
    @Operation(
            summary = "Xoá hồ sơ tổ chức",
            description = "Xoá hồ sơ tổ chức theo `id`. Chỉ ADMIN được phép thực hiện."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteOrganization(
            @Parameter(description = "ID của organization") @PathVariable Long id
    ) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(BaseResponse.noContent("Xóa profile thành công"));
    }

    @PostMapping("/bank-info")
    @Operation(
            summary = "Thêm tài khoản ngân hàng",
            description = "Thêm thông tin tài khoản ngân hàng cho organization hiện tại (lấy `organizationId` từ JWT)."
    )
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
    @Operation(
            summary = "Xoá tài khoản ngân hàng",
            description = "Xoá thông tin tài khoản ngân hàng theo `bankInfoId` của organization hiện tại."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> deleteBankInfo(
            @Parameter(description = "ID của bank info") @PathVariable Long bankInfoId) {

        TokenMetaData tokenMetaData = jwtUtil.getDataFromAuth();
        Long organizerId = tokenMetaData.organizationId();
        if(!tokenMetaData.isOrganization() || organizerId == null || organizerId <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thao tác trên hồ sơ này");
        }

        OrganizationProfileResponse response = organizationService.deleteBankInfo(organizerId, bankInfoId);
        return ResponseEntity.ok(BaseResponse.ok("Xóa tài khoản ngân hàng thành công", response));
    }

    @GetMapping("/bank-info")
    @Operation(
            summary = "Danh sách tài khoản ngân hàng",
            description = "Lấy danh sách thông tin tài khoản ngân hàng của organization hiện tại."
    )
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
    @Operation(
            summary = "Duyệt/đánh giá hồ sơ tổ chức",
            description = "ADMIN xác minh hồ sơ tổ chức theo `id` và cập nhật trạng thái/ghi chú theo request."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> verifyOrganization(
            @Parameter(description = "ID của organization") @PathVariable Long id,
            @Valid @RequestBody VerifyOrganizationRequest request
            ) {
        OrganizationProfileResponse response = organizationService.verifyOrganization(id, request);
        return ResponseEntity.ok(BaseResponse.ok("verify hồ sơ thành công", response));
    }

    @GetMapping("/advanced-search")
    @Operation(
            summary = "Tìm kiếm nâng cao hồ sơ tổ chức",
            description = "Tìm kiếm hồ sơ tổ chức theo trạng thái/tỉnh/thành/từ khoá và phân trang + sắp xếp."
    )
    public ResponseEntity<BasePageResponse<OrganizationProfileResponse>> advancedSearch(
            @Parameter(description = "Trạng thái hồ sơ (optional)") @RequestParam(required = false) OrganizationStatus status,
            @Parameter(description = "Mã tỉnh/thành (provinceCode) (optional)") @RequestParam(required = false) Integer provinceCode,
            @Parameter(description = "Từ khoá tìm kiếm (optional)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trang hiện tại (bắt đầu từ 1)") @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Số phần tử mỗi trang") @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Field dùng để sort (ví dụ: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sort (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));

        Page<OrganizationProfileResponse> response = organizationService.advancedSearch(
                status, provinceCode, keyword, pageable
        );
        return ResponseEntity.ok(BasePageResponse.fromPage(response));
    }
}
