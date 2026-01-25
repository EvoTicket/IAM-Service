package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.event.OtpEvent;
import com.capstone.iamservice.dto.request.ResetPasswordRequest;
import com.capstone.iamservice.dto.request.SendOtpRequest;
import com.capstone.iamservice.dto.request.VerifyOtpRequest;
import com.capstone.iamservice.dto.response.OtpResponse;
import com.capstone.iamservice.entity.OtpRateLimit;
import com.capstone.iamservice.entity.OtpToken;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.OtpType;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.producer.RedisStreamProducer;
import com.capstone.iamservice.repository.OtpRateLimitRepository;
import com.capstone.iamservice.repository.OtpTokenRepository;
import com.capstone.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final OtpRateLimitRepository otpRateLimitRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisStreamProducer redisStreamProducer;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RATE_LIMIT_MINUTES = 3;
    private static final int MAX_ATTEMPTS_PER_HOUR = 5;
    private static final int OTP_LENGTH = 6;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    @Transactional
    public OtpResponse sendOtpForPasswordReset(SendOtpRequest request, OtpType otpType) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Email không tồn tại trong hệ thống"));

        checkRateLimitAndBruteForce(email);

        String otpCode = generateOtpCode();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(7));
        LocalDateTime expiryTime = now.plusMinutes(OTP_EXPIRY_MINUTES);

        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtpCode(otpCode);
        otpToken.setExpiryTime(expiryTime);
        otpToken.setUsed(false);
        otpToken.setType(otpType);
        otpToken = otpTokenRepository.save(otpToken);

        updateRateLimit(email);

        sendOtpEmail(user, otpCode, otpType);

        OtpRateLimit rateLimit = otpRateLimitRepository.findByEmail(email).orElse(null);
        int remainingAttempts = rateLimit != null ? MAX_ATTEMPTS_PER_HOUR - rateLimit.getAttemptCount()
                : MAX_ATTEMPTS_PER_HOUR - 1;

        return OtpResponse.builder()
                .otpId(otpToken.getId())
                .email(email)
                .message("Mã OTP đã được gửi đến email của bạn")
                .expiryTime(expiryTime)
                .remainingAttempts(remainingAttempts)
                .build();
    }

    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request, OtpType otpType) {
        String email = request.getEmail();
        String otpCode = request.getOtpCode();

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, otpType)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy mã OTP hợp lệ"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(7));

        if (now.isAfter(otpToken.getExpiryTime())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP đã hết hạn");
        }

        if (otpToken.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            otpToken.setUsed(true); // Vô hiệu hóa token
            otpTokenRepository.save(otpToken);
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng yêu cầu mã mới.");
        }

        if (!otpToken.getOtpCode().equals(otpCode)) {
            otpToken.setVerificationAttempts(otpToken.getVerificationAttempts() + 1);
            otpTokenRepository.save(otpToken);
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP không chính xác");
        }

        return OtpResponse.builder()
                .otpId(otpToken.getId())
                .email(email)
                .message("Xác thực OTP thành công")
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, OtpType otpType) {
        String email = request.getEmail();
        String otpCode = request.getOtpCode();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Email không tồn tại trong hệ thống"));

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, otpType)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy mã OTP hợp lệ"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(7));

        if (now.isAfter(otpToken.getExpiryTime())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP đã hết hạn");
        }

        if (otpToken.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng yêu cầu mã mới.");
        }

        if (!otpToken.getOtpCode().equals(otpCode)) {
            otpToken.setVerificationAttempts(otpToken.getVerificationAttempts() + 1);
            otpTokenRepository.save(otpToken);
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP không chính xác");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRateLimitRepository.findByEmail(email).ifPresent(otpRateLimitRepository::delete);
    }

    private void checkRateLimitAndBruteForce(String email) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(7));

        OtpRateLimit rateLimit = otpRateLimitRepository.findByEmail(email).orElse(null);

        if (rateLimit != null) {
            long minutesSinceLastSent = ChronoUnit.MINUTES.between(rateLimit.getLastSentAt(), now);
            if (minutesSinceLastSent < RATE_LIMIT_MINUTES) {
                long remainingMinutes = RATE_LIMIT_MINUTES - minutesSinceLastSent;
                throw new AppException(ErrorCode.TOO_MANY_REQUESTS,
                        "Vui lòng đợi " + remainingMinutes + " phút trước khi gửi lại mã OTP");
            }

            long hoursSinceWindowStart = ChronoUnit.HOURS.between(rateLimit.getHourWindowStart(), now);

            if (hoursSinceWindowStart >= 1) {
                rateLimit.setAttemptCount(0);
                rateLimit.setHourWindowStart(now);
            } else if (rateLimit.getAttemptCount() >= MAX_ATTEMPTS_PER_HOUR) {
                long remainingMinutes = 60 - ChronoUnit.MINUTES.between(rateLimit.getHourWindowStart(), now);
                throw new AppException(ErrorCode.TOO_MANY_REQUESTS,
                        "Bạn đã vượt quá số lần gửi OTP cho phép. Vui lòng thử lại sau " + remainingMinutes + " phút");
            }
        }
    }

    private void updateRateLimit(String email) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(7));

        OtpRateLimit rateLimit = otpRateLimitRepository.findByEmail(email).orElse(null);

        if (rateLimit == null) {
            rateLimit = new OtpRateLimit();
            rateLimit.setEmail(email);
            rateLimit.setLastSentAt(now);
            rateLimit.setAttemptCount(1);
            rateLimit.setHourWindowStart(now);
            rateLimit.setCreatedAt(now);
        } else {
            long hoursSinceWindowStart = ChronoUnit.HOURS.between(rateLimit.getHourWindowStart(), now);

            if (hoursSinceWindowStart >= 1) {
                rateLimit.setAttemptCount(1);
                rateLimit.setHourWindowStart(now);
            } else {
                rateLimit.setAttemptCount(rateLimit.getAttemptCount() + 1);
            }
            rateLimit.setLastSentAt(now);
        }

        otpRateLimitRepository.save(rateLimit);
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    private void sendOtpEmail(User user, String otpCode, OtpType otpType) {

        OtpEvent event = OtpEvent.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .otpCode(otpCode)
                .build();

        String streamKey = otpType == OtpType.FORGOT_PASSWORD ? "forgot-password-otp" : "reset-password-otp";
        redisStreamProducer.sendMessage(streamKey, event);
    }
}
