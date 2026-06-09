// CreateRefundRequest.java
package com.pranav.payment_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateRefundRequest {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "1.00", message = "Minimum refund amount is 1.00")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    private String reason;
}