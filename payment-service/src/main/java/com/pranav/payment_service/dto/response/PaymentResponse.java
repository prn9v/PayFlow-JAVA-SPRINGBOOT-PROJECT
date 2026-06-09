// PaymentResponse.java
package com.pranav.payment_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private Long merchantId;
    private String paymentReference;
    private String merchantOrderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}