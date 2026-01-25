package com.capstone.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_rate_limit")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class OtpRateLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String email;

    @Column(nullable = false)
    private LocalDateTime lastSentAt = LocalDateTime.now();

    @Column(nullable = false)
    private Integer attemptCount = 1;

    @Column(nullable = false)
    private LocalDateTime hourWindowStart = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
