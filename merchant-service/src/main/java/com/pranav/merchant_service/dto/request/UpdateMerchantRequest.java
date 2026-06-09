package com.pranav.merchant_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateMerchantRequest {

    @Size(min = 2, max = 100)
    private String businessName;

    @Email
    private String businessEmail;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String businessPhone;

    private String website;

    private String businessAddress;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number")
    private String panNumber;
}