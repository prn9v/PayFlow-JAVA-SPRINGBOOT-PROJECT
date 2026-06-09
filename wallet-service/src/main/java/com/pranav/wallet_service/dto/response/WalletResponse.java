// WalletResponse.java
package com.pranav.wallet_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long       walletId;
    private Long       merchantId;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private String     currency;
    private Boolean    active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}