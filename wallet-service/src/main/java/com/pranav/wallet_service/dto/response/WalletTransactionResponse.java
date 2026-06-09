// WalletTransactionResponse.java
package com.pranav.wallet_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long       id;
    private Long       walletId;
    private Long       merchantId;
    private String     referenceId;
    private String     referenceType;
    private String     transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String     description;
    private LocalDateTime createdAt;
}