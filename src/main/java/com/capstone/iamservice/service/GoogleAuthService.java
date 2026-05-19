package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.AuthenticationResponse;
import com.capstone.iamservice.dto.response.GoogleUserInfo;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.AuthProvider;
import com.capstone.iamservice.enums.RoleEnum;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final WebClient webClient;
    private final RefreshTokenService refreshTokenService;

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @Value("${app.default.avatarUrl}")
    private String defaultAvatarUrl;

    @Transactional
    public AuthenticationResponse processGoogleLogin(String accessToken) {
        // 1. Dùng access_token gọi Google UserInfo API để lấy thông tin user
        GoogleUserInfo googleUserInfo = fetchGoogleUserInfo(accessToken);

        // 2. Kiểm tra email đã verify chưa (bảo mật)
        if (Boolean.FALSE.equals(googleUserInfo.getEmailVerified())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Email Google chưa được xác minh");
        }

        String email = googleUserInfo.getEmail();

        // 3. Tìm user trong DB theo email, nếu chưa có thì tự động tạo mới
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Google login: user đã tồn tại, email={}", email);
        } else {
            user = createGoogleUser(googleUserInfo);
            log.info("Google login: tạo user mới từ Google, email={}", email);
        }

        // 4. Kiểm tra trạng thái tài khoản
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        // 5. Sinh JWT token của hệ thống và trả về (giống login thường)
        String jwtToken     = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private GoogleUserInfo fetchGoogleUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(GOOGLE_USERINFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        log.warn("Google token không hợp lệ, status={}", response.statusCode());
                        return response.createException().map(ex ->
                                new AppException(ErrorCode.UNAUTHORIZED,
                                        "Google access token không hợp lệ hoặc đã hết hạn"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.createException().map(ex ->
                                    new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                                            "Google server lỗi, vui lòng thử lại sau")))
                    .bodyToMono(GoogleUserInfo.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.warn("Google token không hợp lệ, status={}", e.getStatusCode());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Google access token không hợp lệ hoặc đã hết hạn");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi gọi Google UserInfo API", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể kết nối đến Google, vui lòng thử lại");
        }
    }

    private User createGoogleUser(GoogleUserInfo info) {
        String firstName = info.getGivenName() != null ? info.getGivenName() : "";
        String lastName  = info.getFamilyName() != null ? info.getFamilyName() : info.getName();
        String avatar    = info.getPicture() != null ? info.getPicture() : defaultAvatarUrl;

        User user = User.builder()
                .email(info.getEmail())
                .password(null)
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatar)
                .authProvider(AuthProvider.GOOGLE)
                .status(UserStatus.ACTIVE)
                .roles(Set.of(RoleEnum.USER))
                .phoneNumber("")
                .userAddress("")
                .build();

        return userRepository.saveAndFlush(user);
    }
}
