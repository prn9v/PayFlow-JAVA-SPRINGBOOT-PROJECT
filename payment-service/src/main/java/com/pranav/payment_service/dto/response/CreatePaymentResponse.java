package com.pranav.payment_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    private UUID       paymentId;
    private String     paymentReference;
    private String     status;
    private BigDecimal amount;
    private String     currency;

    // ── Razorpay fields needed by frontend checkout ───────────────────────────
    private String     razorpayOrderId;   // Pass to Razorpay JS checkout
    private String     razorpayKeyId;     // Public key for Razorpay checkout
    private BigDecimal amountInPaise;     // Razorpay uses paise (amount * 100)
}