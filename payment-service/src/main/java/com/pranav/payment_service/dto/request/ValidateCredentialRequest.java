// ValidateCredentialRequest.java
package com.pranav.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateCredentialRequest {

    @NotBlank
    private String publicKey;

    @NotBlank
    private String secretKey;
}