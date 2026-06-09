package com.pranav.payment_service.entity;

import com.pranav.payment_service.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "refunds",
        indexes = {
                @Index(name = "idx_refund_payment_id",   columnList = "payment_id"),
                @Index(name = "idx_refund_merchant_id",  columnList = "merchant_id"),
                @Index(name = "idx_refund_reference",    columnList = "refund_reference")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "refund_reference", nullable = false, unique = true)
    private String refundReference;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = RefundStatus.PENDING;
    }
}