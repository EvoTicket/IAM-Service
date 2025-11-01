package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.request.AuthenticationRequest;
import com.capstone.iamservice.dto.response.AuthenticationResponse;
import com.capstone.iamservice.dto.request.RegisterRequest;
import com.capstone.iamservice.entity.Role;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.enums.UserStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.jwt.JwtService;
import com.capstone.iamservice.repository.RoleRepository;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.util.LocationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LocationUtil locationUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${app.default.avatarUrl}")
    String defaultAvatarUrl;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.CONFLICT, "Email đã tồn tại");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy role USER"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .roles(Set.of(userRole))
                .gender(request.getGender())
                .status(UserStatus.ACTIVE)
                .province(locationUtil.getProvinceByCode(request.getProvinceCode()))
                .ward(locationUtil.getWardByCode(request.getWardCode()))
                .avatarUrl(defaultAvatarUrl)
                .build();

        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy user"));

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}