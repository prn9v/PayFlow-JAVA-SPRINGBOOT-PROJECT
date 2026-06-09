// PaymentSuccessEvent.java
package com.pranav.wallet_service.kafka.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID   paymentId;
    private Long   merchantId;
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private String customerEmail;
}