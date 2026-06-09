package com.pranav.merchant_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddBankAccountRequest {

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid account number")
    private String accountNumber;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private Boolean primaryAccount = false;
}