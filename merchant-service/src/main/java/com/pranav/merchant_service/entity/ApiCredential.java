package com.pranav.merchant_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "api_credentials",
        indexes = {
                @Index(name = "idx_api_merchant_id", columnList = "merchant_id"),
                @Index(name = "idx_api_public_key", columnList = "public_key")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "public_key", nullable = false, unique = true)
    private String publicKey;

    @Column(name = "secret_key_hash", nullable = false)
    private String secretKeyHash;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_rotated_at")
    private LocalDateTime lastRotatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        createdAt = LocalDateTime.now();

        if (active == null) {
            active = true;
        }
    }
}