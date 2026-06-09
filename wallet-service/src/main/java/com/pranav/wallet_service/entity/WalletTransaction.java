package com.pranav.wallet_service.entity;

import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet_transactions",
        indexes = {
                @Index(name = "idx_wtxn_wallet_id",     columnList = "wallet_id"),
                @Index(name = "idx_wtxn_merchant_id",   columnList = "merchant_id"),
                @Index(name = "idx_wtxn_reference_id",  columnList = "reference_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    // paymentId, refundId, or settlementId
    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private ReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}