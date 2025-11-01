package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.*;
import com.capstone.iamservice.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Validated
@Tag(name = "Otp service", description = "các tính năng liên quan đến otp")
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/forgot-password/send-otp")
    @Operation(
            summary = "Gửi OTP quên mật khẩu",
            description = "Gửi mã OTP đến email người dùng để thực hiện bước quên mật khẩu."
    )
    @ApiResponse(responseCode = "200", description = "OTP đã được gửi thành công")
    public ResponseEntity<BaseResponse<Void>> sendForgotPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
        BaseResponse<Void> response = otpService.sendForgotPasswordOtp(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/verify-otp")
    @Operation(
            summary = "Xác minh OTP quên mật khẩu",
            description = "Kiểm tra mã OTP được gửi đến email có hợp lệ hay không."
    )
    @ApiResponse(responseCode = "200", description = "OTP xác minh thành công")
    public ResponseEntity<BaseResponse<Void>> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequest request) {
        BaseResponse<Void> response = otpService.verifyForgotPasswordOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/reset")
    @Operation(
            summary = "Đặt lại mật khẩu",
            description = "Đặt lại mật khẩu mới sau khi xác minh OTP thành công."
    )
    @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        BaseResponse<Void> response = otpService.resetPassword(
                request.getEmail(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(response);
    }

    // ====================== XÁC THỰC EMAIL ======================

    @PostMapping("/email-verification/send-otp")
    @Operation(
            summary = "Gửi OTP xác thực email",
            description = "Gửi mã OTP đến email người dùng để xác thực email."
    )
    @ApiResponse(responseCode = "200", description = "OTP đã được gửi thành công")
    public ResponseEntity<BaseResponse<Void>> sendEmailVerificationOtp(@Valid @RequestBody SendOtpRequest request) {
        BaseResponse<Void> response = otpService.sendEmailVerificationOtp(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-verification/verify")
    @Operation(
            summary = "Xác minh email",
            description = "Xác minh OTP đã gửi đến email để hoàn tất xác thực."
    )
    @ApiResponse(responseCode = "200", description = "Xác thực email thành công")
    public ResponseEntity<BaseResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        BaseResponse<Void> response = otpService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(response);
    }

    // ====================== ĐỔI MẬT KHẨU ======================

    @PostMapping("/change-password/send-otp")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Gửi OTP đổi mật khẩu",
            description = "Người dùng đã đăng nhập có thể yêu cầu gửi OTP để đổi mật khẩu."
    )
    @ApiResponse(responseCode = "200", description = "OTP đã được gửi thành công")
    public ResponseEntity<BaseResponse<Void>> requestChangePassword(Authentication authentication) {
        String email = authentication.getName();
        BaseResponse<Void> response = otpService.requestChangePassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password/verify-otp")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Xác minh OTP đổi mật khẩu",
            description = "Người dùng nhập OTP để xác minh trước khi đổi mật khẩu."
    )
    @ApiResponse(responseCode = "200", description = "OTP hợp lệ, có thể tiếp tục đổi mật khẩu")
    public ResponseEntity<BaseResponse<Void>> verifyChangePassword(
            @RequestBody VerifyOtpRequest verifyOtpRequest,
            Authentication authentication) {
        String email = authentication.getName();
        BaseResponse<Void> response = otpService.verifyPasswordResetOtp(email, verifyOtpRequest.getOtpCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Xác nhận đổi mật khẩu",
            description = "Người dùng nhập OTP và mật khẩu mới để hoàn tất quá trình đổi mật khẩu."
    )
    @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công")
    public ResponseEntity<BaseResponse<Void>> confirmChangePassword(
            @Valid @RequestBody ConfirmChangePasswordRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        BaseResponse<Void> response = otpService.confirmChangePassword(
                email,
                request.getNewPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(response);
    }
}