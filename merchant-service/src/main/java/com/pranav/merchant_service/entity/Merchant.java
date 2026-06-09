package com.pranav.merchant_service.entity;

import com.pranav.merchant_service.enums.MerchantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "merchants",
        indexes = {
                @Index(name = "idx_merchant_user_id", columnList = "user_id"),
                @Index(name = "idx_merchant_business_email", columnList = "business_email"),
                @Index(name = "idx_merchant_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @NotBlank
    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Email
    @NotBlank
    @Column(name = "business_email", nullable = false, unique = true)
    private String businessEmail;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid mobile number"
    )
    @Column(name = "business_phone", nullable = false)
    private String businessPhone;

    @Column(name = "website")
    private String website;

    @Column(name = "business_address", length = 1000)
    private String businessAddress;

    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number"
    )
    @Column(name = "pan_number", unique = true)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status;

    @Column(name = "kyc_verified", nullable = false)
    private Boolean kycVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = MerchantStatus.PENDING;
        }

        if (kycVerified == null) {
            kycVerified = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}