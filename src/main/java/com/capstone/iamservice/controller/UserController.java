package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.response.UserResponse;
import com.capstone.iamservice.security.JwtUtil;
import com.capstone.iamservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API quản lý người dùng")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<BasePageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(BaseResponse.ok("Lấy thông tin người dùng thành công", BasePageResponse.fromPage(users)));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByEmail(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse userResponse = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy thông tin người dùng thành công", userResponse));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<UserResponse>> uploadUserAvatar(
            @Parameter(
                    description = "File ảnh đại diện",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = jwtUtil.getDataFromAuth().userId();
        UserResponse user = userService.uploadAvatar(file, userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy thông tin người dùng thành công", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.noContent("Xóa người dùng thành công"));
    }
}