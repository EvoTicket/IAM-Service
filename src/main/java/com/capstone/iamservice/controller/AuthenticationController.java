package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.AuthenticationRequest;
import com.capstone.iamservice.dto.request.GoogleLoginRequest;
import com.capstone.iamservice.dto.request.RefreshTokenRequest;
import com.capstone.iamservice.dto.response.AuthenticationResponse;
import com.capstone.iamservice.dto.request.RegisterRequest;
import com.capstone.iamservice.service.AuthenticationService;
import com.capstone.iamservice.service.GoogleAuthService;
import com.capstone.iamservice.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực và phân quyền")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final GoogleAuthService googleAuthService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<BaseResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created("Đăng ký thành công", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không chính xác")
    })
    public ResponseEntity<BaseResponse<AuthenticationResponse>> login(
            @Valid @RequestBody AuthenticationRequest request) {

        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Đăng nhập thành công", response));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập Google thành công"),
            @ApiResponse(responseCode = "401", description = "Access token Google không hợp lệ")
    })
    public ResponseEntity<BaseResponse<AuthenticationResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {

        AuthenticationResponse response = googleAuthService.processGoogleLogin(request.getAccessToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Đăng nhập Google thành công", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới access token",
            description = "Dùng refresh token để cấp lại access token mới. Refresh token cũ sẽ bị huỷ và một refresh token mới được cấp (token rotation).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Làm mới token thành công"),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc đã hết hạn")
    })
    public ResponseEntity<BaseResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthenticationResponse response = refreshTokenService.refresh(request.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Thu hồi refresh token, buộc client phải đăng nhập lại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "400", description = "Refresh token không tồn tại")
    })
    public ResponseEntity<BaseResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Đăng xuất thành công", null));
    }
}