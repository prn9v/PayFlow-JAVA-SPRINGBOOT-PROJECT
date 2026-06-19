// RefundCreatedEvent.java
package com.pranav.wallet_service.rabbitmq.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCreatedEvent {
    private Long    refundId;
    private UUID paymentId;
    private Long      merchantId;
    private String    refundReference;
    private BigDecimal amount;
    private String    reason;
    private String    customerEmail;
}