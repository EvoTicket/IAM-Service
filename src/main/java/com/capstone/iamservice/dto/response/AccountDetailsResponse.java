package com.capstone.iamservice.dto.response;

import com.capstone.iamservice.enums.RoleEnum;
import com.capstone.iamservice.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountDetailsResponse {
    private Long id;
    private String email;
    private String fullName;
    private RoleEnum role;
    private String phoneNumber;
    private String verificationStatus;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;
}
