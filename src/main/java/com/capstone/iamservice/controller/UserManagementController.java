package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.response.AccountDetailResponse;
import com.capstone.iamservice.dto.response.AccountDetailsResponse;
import com.capstone.iamservice.dto.response.AccountSummaryResponse;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserManagementService userManagementService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<AccountSummaryResponse>> getAccountSummary() {
        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy thông tin tổng quan tài khoản thành công",
                userManagementService.getAccountSummary()
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BasePageResponse<AccountDetailsResponse>> searchAccounts(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) OrganizationStatus verification,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<AccountDetailsResponse> result = userManagementService.searchAccounts(
                role, status, verification, keyword, days, pageable
        );

        return ResponseEntity.ok(BasePageResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<AccountDetailResponse>> getAccountDetail(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết tài khoản thành công",
                userManagementService.getAccountDetail(id)
        ));
    }

    @GetMapping("/checkers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BasePageResponse<AccountDetailsResponse>> getCheckers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<AccountDetailsResponse> result = userManagementService.getCheckers(pageable);
        return ResponseEntity.ok(BasePageResponse.ok(result));
    }
}
