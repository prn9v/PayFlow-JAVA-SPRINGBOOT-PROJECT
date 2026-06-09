// CreatePaymentRequest.java
package com.pranav.payment_service.dto.request;

import com.pranav.payment_service.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    @NotBlank(message = "Public key is required")
    private String publicKey;

    @NotBlank(message = "Secret key is required")
    private String secretKey;

    @NotBlank(message = "Merchant order ID is required")
    private String merchantOrderId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum amount is 1.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters e.g. INR")
    private String currency;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid customer email")
    private String customerEmail;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid customer phone")
    private String customerPhone;

    private String description;

    private PaymentMethod paymentMethod;
}