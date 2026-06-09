package com.pranav.merchant_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {

    private String accountHolderName;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String ifscCode;

    private String bankName;
}