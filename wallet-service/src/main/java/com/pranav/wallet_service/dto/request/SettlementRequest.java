// SettlementRequest.java
package com.pranav.wallet_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SettlementRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum settlement amount is 1.00")
    private BigDecimal amount;
}