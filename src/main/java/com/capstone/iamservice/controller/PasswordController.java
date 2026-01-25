package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.ResetPasswordRequest;
import com.capstone.iamservice.dto.request.SendOtpRequest;
import com.capstone.iamservice.dto.request.VerifyOtpRequest;
import com.capstone.iamservice.dto.response.OtpResponse;
import com.capstone.iamservice.enums.OtpType;
import com.capstone.iamservice.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@Tag(name = "Password Management", description = "API quản lý mật khẩu - Quên mật khẩu và đổi mật khẩu")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot-password/send-otp")
    @Operation(summary = "Gửi OTP để quên mật khẩu", description = "Gửi mã OTP đến email để khôi phục mật khẩu. OTP có hiệu lực 5 phút. "
            +
            "Rate limit: 3 phút. Brute force protection: 5 lần/giờ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi OTP thành công"),
            @ApiResponse(responseCode = "404", description = "Email không tồn tại"),
            @ApiResponse(responseCode = "429", description = "Quá nhiều yêu cầu")
    })
    public ResponseEntity<BaseResponse<OtpResponse>> sendOtpForForgotPassword(
            @Valid @RequestBody SendOtpRequest request) {

        OtpResponse response = passwordService.sendOtpForPasswordReset(request, OtpType.FORGOT_PASSWORD);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Mã OTP đã được gửi đến email của bạn", response));
    }

    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Xác thực OTP cho quên mật khẩu", description = "Xác thực mã OTP đã gửi đến email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực OTP thành công"),
            @ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy OTP")
    })
    public ResponseEntity<BaseResponse<OtpResponse>> verifyOtpForForgotPassword(
            @Valid @RequestBody VerifyOtpRequest request) {

        OtpResponse response = passwordService.verifyOtp(request, OtpType.FORGOT_PASSWORD);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Xác thực OTP thành công", response));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Đặt lại mật khẩu sau khi quên", description = "Đặt lại mật khẩu mới sau khi xác thực OTP thành công")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc OTP đã hết hạn"),
            @ApiResponse(responseCode = "404", description = "Email hoặc OTP không tồn tại")
    })
    public ResponseEntity<BaseResponse<Void>> resetPasswordForForgotPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordService.resetPassword(request, OtpType.FORGOT_PASSWORD);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Đặt lại mật khẩu thành công", null));
    }

    @PostMapping("/reset-password/send-otp")
    @Operation(summary = "Gửi OTP để đổi mật khẩu", description = "Gửi mã OTP đến email để đổi mật khẩu. OTP có hiệu lực 5 phút. "
            +
            "Rate limit: 3 phút. Brute force protection: 5 lần/giờ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi OTP thành công"),
            @ApiResponse(responseCode = "404", description = "Email không tồn tại"),
            @ApiResponse(responseCode = "429", description = "Quá nhiều yêu cầu")
    })
    public ResponseEntity<BaseResponse<OtpResponse>> sendOtpForResetPassword(
            @Valid @RequestBody SendOtpRequest request) {

        OtpResponse response = passwordService.sendOtpForPasswordReset(request, OtpType.PASSWORD_RESET);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Mã OTP đã được gửi đến email của bạn", response));
    }

    @PostMapping("/reset-password/verify-otp")
    @Operation(summary = "Xác thực OTP cho đổi mật khẩu", description = "Xác thực mã OTP đã gửi đến email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực OTP thành công"),
            @ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy OTP")
    })
    public ResponseEntity<BaseResponse<OtpResponse>> verifyOtpForResetPassword(
            @Valid @RequestBody VerifyOtpRequest request) {

        OtpResponse response = passwordService.verifyOtp(request, OtpType.PASSWORD_RESET);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Xác thực OTP thành công", response));
    }

    @PostMapping("/reset-password/reset")
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu mới sau khi xác thực OTP thành công")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc OTP đã hết hạn"),
            @ApiResponse(responseCode = "404", description = "Email hoặc OTP không tồn tại")
    })
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordService.resetPassword(request, OtpType.PASSWORD_RESET);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Đổi mật khẩu thành công", null));
    }
}
