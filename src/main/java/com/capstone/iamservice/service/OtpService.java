package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.event.OtpEvent;
import com.capstone.iamservice.entity.OtpToken;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.OtpTokenRepository;
import com.capstone.iamservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, OtpEvent> kafkaTemplate;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.cooldown-minutes}")
    private int cooldownMinutes;

    private final SecureRandom random = new SecureRandom();

    private String createOtp(String email, OtpToken.OtpType otpType) {
        if (!userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Email không tồn tại trong hệ thống");
        }

        Optional<OtpToken> existing = otpTokenRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        email, otpType);

        if (existing.isPresent() && existing.get().getCreatedAt()
                .isAfter(OffsetDateTime.now(ZoneOffset.ofHours(7)).minusMinutes(cooldownMinutes))) {
            throw new AppException(ErrorCode.WAITING_FOR_OTP);
        }

        // Vô hiệu hóa tất cả OTP cũ của email này
        otpTokenRepository.markAllAsUsedByEmailAndType(email, otpType);

        // Tạo mã OTP mới
        String otpCode = generateRanDomOtpCode();

        // Lưu OTP vào database
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtpCode(otpCode);
        otpToken.setType(otpType);
        otpToken.setExpiryTime(OffsetDateTime.now(ZoneOffset.ofHours(7)).plusMinutes(otpExpirationMinutes));
        otpTokenRepository.save(otpToken);

        return otpCode;
    }

    // ====================== QUÊN MẬT KHẨU ======================

    @Transactional
    public BaseResponse<Void> sendForgotPasswordOtp(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.FORGOT_PASSWORD);

        OtpEvent event = OtpEvent.builder()
                .email(email)
                .otpCode(otpCode)
                .build();
        kafkaTemplate.send("otp-topic", event);

        return BaseResponse.ok("Mã OTP đã được gửi đến email của bạn", null);
    }

    public BaseResponse<Void> verifyForgotPasswordOtp(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, OffsetDateTime.now(ZoneOffset.ofHours(7)));

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.FORGOT_PASSWORD) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return BaseResponse.ok("Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    @Transactional
    public BaseResponse<Void> resetPassword(String email, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return BaseResponse.badRequest("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thầy người dùng với email" + email));
        user.setPassword(passwordEncoder.encode(newPassword));

        return BaseResponse.ok("Đặt lại mật khẩu thành công", null);

    }

    // ====================== XÁC THỰC EMAIL ======================

    @Transactional
    public BaseResponse<Void> sendEmailVerificationOtp(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.EMAIL_VERIFICATION);
        emailService.sendEmailVerificationOtp(email, otpCode);
        return BaseResponse.ok("Mã OTP xác thực đã được gửi đến email của bạn", null);
    }

    @Transactional
    public BaseResponse<Void> verifyEmail(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, OffsetDateTime.now(ZoneOffset.ofHours(7)));

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.EMAIL_VERIFICATION) {
                token.setUsed(true);
                otpTokenRepository.save(token);

                return BaseResponse.ok("Xác thực email thành công", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    // ====================== ĐỔI MẬT KHẨU ======================

    @Transactional
    public BaseResponse<Void> requestChangePassword(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.PASSWORD_RESET);
        emailService.sendChangePasswordOtp(email, otpCode);
        return BaseResponse.ok("Mã OTP đã được gửi đến email của bạn để xác nhận đổi mật khẩu", null);
    }

    public BaseResponse<Void> verifyPasswordResetOtp(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, OffsetDateTime.now(ZoneOffset.ofHours(7)));

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.PASSWORD_RESET) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return BaseResponse.ok("Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    @Transactional
    public BaseResponse<Void> confirmChangePassword(String email, String newPassword, String confirmPassword) {
        // Kiểm tra mật khẩu xác nhận
        if (!newPassword.equals(confirmPassword)) {
            return BaseResponse.badRequest("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thầy người dùng với email" + email));
        user.setPassword(passwordEncoder.encode(newPassword));

        return BaseResponse.ok("Đổi mật khẩu thành công", null);
    }

    private String generateRanDomOtpCode() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredTokens(OffsetDateTime.now(ZoneOffset.ofHours(7)));
    }
}
