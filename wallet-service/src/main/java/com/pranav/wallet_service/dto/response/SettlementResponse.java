// SettlementResponse.java
package com.pranav.wallet_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long       settlementId;
    private Long       merchantId;
    private Long       walletId;
    private BigDecimal amount;
    private String     status;
    private String     bankReference;
    private LocalDateTime createdAt;
}