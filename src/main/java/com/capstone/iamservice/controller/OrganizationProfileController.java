package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.CreateOrganizationRequest;
import com.capstone.iamservice.dto.response.OrganizationProfileResponse;
import com.capstone.iamservice.dto.request.UpdateOrganizationRequest;
import com.capstone.iamservice.dto.request.VerifyOrganizationRequest;
import com.capstone.iamservice.dto.response.UserResponse;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.service.OrganizationProfileService;
import com.capstone.iamservice.util.TokenMetaData;
import com.capstone.iamservice.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationProfileController {

    private final OrganizationProfileService organizationService;
    private final UserUtil userUtil;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication) {

        Long userId = userUtil.getDataFromAuth(authentication).userId();

        OrganizationProfileResponse response = organizationService.createOrganization(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.created("tạo org profile thành công",response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationProfileResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getMyOrganization(Authentication authentication) {
        Long userId = userUtil.getDataFromAuth(authentication).userId();
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> getOrganizationByUserId(@PathVariable Long userId) {
        OrganizationProfileResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(BaseResponse.ok("Lấy profile thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            Authentication authentication) {

        TokenMetaData tokenMetaData = userUtil.getDataFromAuth(authentication);
        if(!tokenMetaData.isOrganization()){
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn chưa có hồ sơ doanh nghiệp");
        }

        OrganizationProfileResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(BaseResponse.ok("Update profile thành công", response));
    }

    @PostMapping(value = "/logo-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload logoUrl")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<OrganizationProfileResponse>> uploadUserAvatar(
            Authentication authentication,
            @Parameter(
                    description = "File ảnh logo",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file
    ) {
        TokenMetaData tokenMetaData = userUtil.getDataFromAuth(authentication);
        if(!tokenMetaData.isOrganization()){
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
