package com.capstone.iamservice.entity;

import com.capstone.iamservice.enums.OrganizationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "organization_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String legalName;

    @Column(unique = true, nullable = false)
    private String taxCode;

    private String logoUrl;

    private String description;

    @Column(nullable = false)
    private String businessAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code", referencedColumnName = "code", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "code",  nullable = false)
    private Province province;

    @Column(length = 10)
    private String businessPhone;

    @Column(length = 255)
    @Email
    private String businessEmail;

    @Column(length = 500)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String businessLicenseUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column
    private OffsetDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
        updatedAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    public String getFullAddress() {
        if (ward != null && province != null) {
            return businessAddress + ", " + ward.getName() + ", " + province.getName();
        }
        return businessAddress;
    }
}