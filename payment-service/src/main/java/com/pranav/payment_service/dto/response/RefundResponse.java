// RefundResponse.java
package com.pranav.payment_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long refundId;
    private UUID paymentId;
    private Long merchantId;
    private String refundReference;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}