package com.capstone.iamservice.config;

import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.enums.Gender;
import com.capstone.iamservice.enums.RoleEnum;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.repository.WardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@Profile({"local", "dev", "test"})
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_BUYER = "BUYER";
    private static final String ROLE_CHECKER = "CHECKER";
    private static final String ROLE_ORGANIZER = "ORGANIZER";
    private static final int LOCAL_SEED_PROVINCE_CODE = 1;
    private static final int LOCAL_SEED_WARD_CODE = 4;

    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.avatarUrl}")
    private String defaultAvatarUrl;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.seed.password:Password@123}")
    private String seedPassword;

    @Override
    @Transactional
    public void run(String... args) {
        initLocationDataIfMissing();
        initUsers();
    }

    private void initLocationDataIfMissing() {
        Province province = provinceRepository.findByCode(LOCAL_SEED_PROVINCE_CODE)
                .orElseGet(this::createLocalSeedProvince);

        if (wardRepository.findByCode(LOCAL_SEED_WARD_CODE).isEmpty()) {
            Ward ward = Ward.builder()
                    .code(LOCAL_SEED_WARD_CODE)
                    .name("Phường Bến Nghé")
                    .divisionType("phường")
                    .codename("phuong_ben_nghe")
                    .province(province)
                    .build();
            wardRepository.save(ward);
            log.info("Created local seed ward with code {}", LOCAL_SEED_WARD_CODE);
        }
    }

    private Province createLocalSeedProvince() {
        Province province = Province.builder()
                .code(LOCAL_SEED_PROVINCE_CODE)
                .name("Thành phố Hồ Chí Minh")
                .divisionType("thành phố trung ương")
                .codename("thanh_pho_ho_chi_minh")
                .phoneCode(28)
                .build();

        Province saved = provinceRepository.save(province);
        log.info("Created local seed province with code {}", LOCAL_SEED_PROVINCE_CODE);
        return saved;
    }

    private void initUsers() {
        createOrUpdateUser(
                adminEmail,
                adminPassword,
                "Admin",
                "User",
                "0123456789",
                Gender.MALE,
                Set.of(RoleEnum.ADMIN, RoleEnum.USER)
        );

        createOrUpdateUser(
                "buyer@example.com",
                seedPassword,
                "Buyer",
                "User",
                "0900000201",
                Gender.FEMALE,
                Set.of(RoleEnum.USER, RoleEnum.BUYER)
        );

        createOrUpdateUser(
                "checker@example.com",
                seedPassword,
                "Checker",
                "Gate",
                "0900000301",
                Gender.MALE,
                Set.of(RoleEnum.USER, RoleEnum.CHECKER)
        );

        createOrUpdateUser(
                "organizer@example.com",
                seedPassword,
                "Organizer",
                "User",
                "0900000401",
                Gender.MALE,
                Set.of(RoleEnum.USER, RoleEnum.ORGANIZER)
        );
    }

    private void createOrUpdateUser(
            String email,
            String rawPassword,
            String firstName,
            String lastName,
            String phoneNumber,
            Gender gender,
            Set<RoleEnum> roles
    ) {
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getRoles() == null) {
                user.setRoles(new java.util.HashSet<>(roles));
                userRepository.save(user);
                log.info("Updated seed user roles for {}", email);
                return;
            }

            boolean changed = user.getRoles().addAll(roles);
            if (changed) {
                userRepository.save(user);
                log.info("Updated seed user roles for {}", email);
            }
            return;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true)
                .roles(roles)
                .phoneNumber(phoneNumber)
                .userAddress("Ho Chi Minh City")
                .avatarUrl(defaultAvatarUrl)
                .dateOfBirth(LocalDate.of(2000, 5, 20))
                .gender(gender)
                .province(provinceRepository.findByCode(LOCAL_SEED_PROVINCE_CODE)
                        .orElseThrow(() -> new AppException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "Local IAM seed requires province code "
                                        + LOCAL_SEED_PROVINCE_CODE
                                        + ". Load location data or disable app.seed.enabled."
                        )))
                .ward(wardRepository.findByCode(LOCAL_SEED_WARD_CODE)
                        .orElseThrow(() -> new AppException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "Local IAM seed requires ward code "
                                        + LOCAL_SEED_WARD_CODE
                                        + ". Load location data or disable app.seed.enabled."
                        )))
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("Created local seed user: {}", email);
    }
}
