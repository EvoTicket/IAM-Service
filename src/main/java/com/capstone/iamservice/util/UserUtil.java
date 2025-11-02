package com.capstone.iamservice.util;

import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy user với ID: " + id
                ));
    }

    public TokenMetaData getDataFromAuth(Authentication authentication) {
        com.capstone.iamservice.entity.User user =
                (com.capstone.iamservice.entity.User) authentication.getPrincipal();
        return new TokenMetaData(
                user.getId(),
                user.isOrganization(),
                user.getOrganizationProfile() == null ? null : user.getOrganizationProfile().getId()
        );
    }
}
