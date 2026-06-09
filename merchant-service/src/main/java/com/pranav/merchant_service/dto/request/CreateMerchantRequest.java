package com.pranav.merchant_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMerchantRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid email format")
    private String businessEmail;

    @NotBlank(message = "Business phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String businessPhone;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number")
    private String panNumber;

    private String website;

    private String businessAddress;
}