package com.pranav.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallets",
        indexes = {
                @Index(name = "idx_wallet_merchant_id", columnList = "merchant_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false, unique = true)
    private Long merchantId;

    @Column(name = "available_balance",
            nullable = false,
            precision = 14,
            scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "pending_balance",
            nullable = false,
            precision = 14,
            scale = 2)
    private BigDecimal pendingBalance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt  = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
        if (pendingBalance   == null) pendingBalance   = BigDecimal.ZERO;
        if (active           == null) active           = true;
        if (currency         == null) currency         = "INR";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}