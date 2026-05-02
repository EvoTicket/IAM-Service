package com.capstone.iamservice.config;

import com.capstone.iamservice.entity.Role;
import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.RoleRepository;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.repository.WardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    @Test
    void runCreatesMissingRolesOnceAndAddsMissingUserRoles() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProvinceRepository provinceRepository = mock(ProvinceRepository.class);
        WardRepository wardRepository = mock(WardRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        Map<String, Role> roles = new HashMap<>();
        when(roleRepository.findByName(any())).thenAnswer(invocation ->
                Optional.ofNullable(roles.get(invocation.getArgument(0, String.class))));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            roles.put(role.getName(), role);
            return role;
        });

        Map<Integer, Province> provinces = new HashMap<>();
        when(provinceRepository.findByCode(any())).thenAnswer(invocation ->
                Optional.ofNullable(provinces.get(invocation.getArgument(0, Integer.class))));
        when(provinceRepository.save(any(Province.class))).thenAnswer(invocation -> {
            Province province = invocation.getArgument(0);
            provinces.put(province.getCode(), province);
            return province;
        });

        Map<Integer, Ward> wards = new HashMap<>();
        when(wardRepository.findByCode(any())).thenAnswer(invocation ->
                Optional.ofNullable(wards.get(invocation.getArgument(0, Integer.class))));
        when(wardRepository.save(any(Ward.class))).thenAnswer(invocation -> {
            Ward ward = invocation.getArgument(0);
            wards.put(ward.getCode(), ward);
            return ward;
        });

        Map<String, User> users = new HashMap<>();
        users.put("admin@evoticket.com", user("admin@evoticket.com"));
        users.put("buyer@example.com", user("buyer@example.com"));
        users.put("checker@example.com", user("checker@example.com"));
        when(userRepository.findByEmail(any())).thenAnswer(invocation ->
                Optional.ofNullable(users.get(invocation.getArgument(0, String.class))));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getEmail(), user);
            return user;
        });

        DataInitializer initializer = new DataInitializer(
                roleRepository,
                userRepository,
                provinceRepository,
                wardRepository,
                passwordEncoder
        );
        ReflectionTestUtils.setField(initializer, "defaultAvatarUrl", "https://example.test/avatar.png");
        ReflectionTestUtils.setField(initializer, "adminEmail", "admin@evoticket.com");
        ReflectionTestUtils.setField(initializer, "adminPassword", "admin-local");
        ReflectionTestUtils.setField(initializer, "seedPassword", "Password@123");

        initializer.run();
        initializer.run();

        assertThat(roles.keySet()).containsExactlyInAnyOrder("ADMIN", "USER", "BUYER", "CHECKER");
        assertThat(provinces).containsOnlyKeys(1);
        assertThat(provinces.get(1).getName()).isEqualTo("Thành phố Hồ Chí Minh");
        assertThat(wards).containsOnlyKeys(4);
        assertThat(wards.get(4).getName()).isEqualTo("Phường Bến Nghé");
        assertThat(wards.get(4).getProvince()).isSameAs(provinces.get(1));
        assertThat(roleNames(users.get("admin@evoticket.com"))).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(roleNames(users.get("buyer@example.com"))).containsExactlyInAnyOrder("USER", "BUYER");
        assertThat(roleNames(users.get("checker@example.com"))).containsExactlyInAnyOrder("USER", "CHECKER");
    }

    private User user(String email) {
        return User.builder()
                .email(email)
                .roles(new HashSet<>())
                .build();
    }

    private java.util.Set<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}
