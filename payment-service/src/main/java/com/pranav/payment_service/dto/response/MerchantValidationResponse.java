// MerchantValidationResponse.java
package com.pranav.payment_service.dto.response;

import lombok.Data;

@Data
public class MerchantValidationResponse {
    private Long merchantId;
    private String merchantStatus;
    private Boolean kycVerified;
    private Boolean valid;
}