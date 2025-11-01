package com.capstone.iamservice.config;

import com.capstone.iamservice.entity.Role;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.Gender;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.RoleRepository;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.repository.WardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.avatarUrl}")
    String defaultAvatarUrl;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Administrator role with full access")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created ADMIN role");
        }

        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("USER")
                    .description("Standard user role")
                    .build();
            roleRepository.save(userRole);
            log.info("Created USER role");
        }
    }

    private void initAdminUser() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User adminUser = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .phoneNumber("0123456789")
                    .userAddress("fgdgdfg")
                    .avatarUrl(defaultAvatarUrl)
                    .dateOfBirth(LocalDate.of(2000, 5, 20))
                    .gender(Gender.MALE)
                    .province(provinceRepository.findByCode(1).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "không tìm thấy tỉnh")))
                    .ward(wardRepository.findByCode(4).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "không tìm thấy phường/xã")))
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(adminUser);
            log.info("Created default admin user: admin@example.com / admin123");
        }
    }
}