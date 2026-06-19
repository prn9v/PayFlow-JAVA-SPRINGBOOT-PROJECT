package com.pranav.payment_service.rabbitmq.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID paymentId;
    private Long      merchantId;
    private String    paymentReference;
    private BigDecimal amount;
    private String    currency;
    private String    customerEmail;
}