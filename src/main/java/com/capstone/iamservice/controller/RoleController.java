package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.CreateRoleRequest;
import com.capstone.iamservice.dto.response.RoleResponse;
import com.capstone.iamservice.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "API quản lý vai trò")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Tạo role mới")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse role = roleService.createRole(request.getName(), request.getDescription());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created("Tạo role mới thành công", role));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách roles")
    public ResponseEntity<BaseResponse<Page<RoleResponse>>> getAllRoles(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RoleResponse> roles = roleService.getAllRoles(pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy danh sách role thành công", roles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin role theo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy thông tin role thành công", role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoleRequest request) {

        RoleResponse role = roleService.updateRole(id, request.getName(), request.getDescription());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Cập nhật role thành công", role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.noContent("Xóa role thành công"));
    }
}