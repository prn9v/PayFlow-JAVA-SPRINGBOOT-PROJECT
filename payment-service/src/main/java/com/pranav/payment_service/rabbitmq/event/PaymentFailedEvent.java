package com.pranav.payment_service.rabbitmq.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private UUID paymentId;
    private Long      merchantId;
    private String    paymentReference;
    private BigDecimal amount;
    private String    reason;
    private String    customerEmail;
}