package com.capstone.iamservice.dto.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEvent {
    String email;
    String otpCode;
}
