package com.capstone.iamservice.entity;

import com.capstone.iamservice.enums.OtpType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private int verificationAttempts = 0;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.ofHours(7));
    }
}