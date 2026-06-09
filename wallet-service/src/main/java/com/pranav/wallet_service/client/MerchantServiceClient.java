// MerchantServiceClient.java
package com.pranav.wallet_service.client;

import com.pranav.wallet_service.config.FeignConfig;
import com.pranav.wallet_service.dto.response.MerchantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "merchant-service",
        configuration = FeignConfig.class
)
public interface MerchantServiceClient {

    @GetMapping("/internal/merchants/{merchantId}/validate")
    Map<String, Object> validateMerchant(
            @PathVariable("merchantId") Long merchantId);

    @GetMapping("/internal/merchants/user/{userId}")
    MerchantResponse getMerchantByUserId(
            @PathVariable Long userId);
}