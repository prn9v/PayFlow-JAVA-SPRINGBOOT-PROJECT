// CreditDebitRequest.java
package com.pranav.wallet_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditDebitRequest {

    @NotNull(message = "Merchant ID is required")
    private Long merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Reference ID is required")
    private String referenceId;

    @NotBlank(message = "Description is required")
    private String description;
}