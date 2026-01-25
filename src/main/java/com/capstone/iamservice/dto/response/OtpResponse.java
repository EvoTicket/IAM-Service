package com.capstone.iamservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {
    private Long otpId;
    private String email;
    private String message;
    private LocalDateTime expiryTime;
    private Integer remainingAttempts;
}
