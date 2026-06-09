package com.pranav.payment_service.entity;

import com.pranav.payment_service.enums.PaymentMethod;
import com.pranav.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_merchant_id",       columnList = "merchant_id"),
                @Index(name = "idx_payment_reference",         columnList = "payment_reference"),
                @Index(name = "idx_payment_status",            columnList = "status"),
                @Index(name = "idx_payment_public_key",        columnList = "public_key"),
                @Index(name = "idx_payment_razorpay_order_id", columnList = "razorpay_order_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference;

    @Column(name = "merchant_order_id", nullable = false)
    private String merchantOrderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone", length = 15)
    private String customerPhone;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "public_key", nullable = false)
    private String publicKey;

    // ── Razorpay fields ───────────────────────────────────────────────────────

    // Razorpay order ID returned when order is created
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;

    // Razorpay payment ID received in webhook after customer pays
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    // Razorpay signature for verification
    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status   == null) status   = PaymentStatus.PENDING;
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}