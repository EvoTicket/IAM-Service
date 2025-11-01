package com.capstone.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "otp_tokens")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private OffsetDateTime expiryTime;

    @Column(nullable = false)
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    public enum OtpType {
        PASSWORD_RESET,
        FORGOT_PASSWORD,
        EMAIL_VERIFICATION
    }
}