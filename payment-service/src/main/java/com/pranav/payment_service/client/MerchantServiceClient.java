// MerchantServiceClient.java
package com.pranav.payment_service.client;

import com.pranav.payment_service.config.FeignConfig;
import com.pranav.payment_service.dto.request.ValidateCredentialRequest;
import com.pranav.payment_service.dto.response.MerchantValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "merchant-service",
        configuration = FeignConfig.class
)
public interface MerchantServiceClient {

    // Validate publicKey + secretKey — returns merchantId, status, kycVerified
    @PostMapping("/internal/merchants/credentials/validate")
    MerchantValidationResponse validateCredentials(
            @RequestBody ValidateCredentialRequest request);

    // Validate merchant is ACTIVE
    @GetMapping("/internal/merchants/{merchantId}/validate")
    MerchantValidationResponse validateMerchant(
            @PathVariable("merchantId") Long merchantId);

    @GetMapping("/internal/merchants/by-user/{userId}")
    Long getMerchantIdByUserId(@PathVariable("userId") Long userId);
}