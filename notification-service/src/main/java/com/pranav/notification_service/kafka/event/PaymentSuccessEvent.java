// PaymentSuccessEvent.java
package com.pranav.notification_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID paymentId;
    private Long merchantId;
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private String customerEmail;
}