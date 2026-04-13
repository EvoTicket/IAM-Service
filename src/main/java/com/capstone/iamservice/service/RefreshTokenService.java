package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.AuthenticationResponse;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    // Prefix để tránh collision với các key khác trong Redis
    private static final String KEY_PREFIX = "refresh_token:";

    @Value("${spring.security.jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationMs;

    /**
     * Tạo refresh token mới (UUID), lưu vào Redis với TTL và trả về.
     * Key: "refresh_token:<token>"  →  Value: email
     */
    public String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        String key   = KEY_PREFIX + token;

        stringRedisTemplate.opsForValue().set(
                key,
                email,
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        log.debug("Refresh token created for email={}", email);
        return token;
    }

    /**
     * Dùng refresh token để cấp lại access token + refresh token mới (rotation).
     * Refresh token cũ bị xóa ngay sau khi dùng (one-time use).
     */
    public AuthenticationResponse refresh(String refreshToken) {
        String key   = KEY_PREFIX + refreshToken;
        String email = stringRedisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    "Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Xóa token cũ ngay lập tức (token rotation — chống replay attack)
        stringRedisTemplate.delete(key);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy tài khoản"));

        // Cấp access token mới + refresh token mới
        String newAccessToken  = jwtService.generateToken(user);
        String newRefreshToken = createRefreshToken(email);

        log.info("Token refreshed for email={}", email);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Thu hồi (logout) — xóa refresh token khỏi Redis.
     */
    public void revoke(String refreshToken) {
        String key     = KEY_PREFIX + refreshToken;
        boolean exists = stringRedisTemplate.hasKey(key);

        if (!exists) throw new AppException(ErrorCode.BAD_REQUEST, "Refresh token không tồn tại hoặc đã hết hạn");

        stringRedisTemplate.delete(key);
        log.info("Refresh token revoked");
    }
}
