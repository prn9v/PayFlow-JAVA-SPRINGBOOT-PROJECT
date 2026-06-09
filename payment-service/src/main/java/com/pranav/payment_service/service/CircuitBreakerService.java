package com.pranav.payment_service.service;

import com.pranav.payment_service.client.MerchantServiceClient;
import com.pranav.payment_service.dto.request.ValidateCredentialRequest;
import com.pranav.payment_service.dto.response.MerchantValidationResponse;
import com.pranav.payment_service.exception.MerchantValidationException;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerService {

    private final MerchantServiceClient merchantServiceClient;
    private final RazorpayClient        razorpayClient;

    // ─── Merchant Service call with Circuit Breaker + Retry ──────────────────

    @CircuitBreaker(name = "merchantService",
            fallbackMethod = "merchantValidationFallback")
    @Retry(name = "merchantService")
    public MerchantValidationResponse validateMerchantCredentials(
            ValidateCredentialRequest request) {
        log.debug("Calling merchant-service for credential validation");
        return merchantServiceClient.validateCredentials(request);
    }

    public MerchantValidationResponse merchantValidationFallback(
            ValidateCredentialRequest request, Throwable throwable) {
        log.error("Merchant service circuit breaker OPEN. Cause: {}",
                throwable.getMessage());
        throw new MerchantValidationException(
                "Merchant Service is currently unavailable. " +
                        "Please try again in a few moments.");
    }

    // ─── Razorpay call with Circuit Breaker + Retry ───────────────────────────

    @CircuitBreaker(name = "razorpayService",
            fallbackMethod = "razorpayOrderFallback")
    @Retry(name = "razorpayService")
    public String createRazorpayOrderWithBreaker(BigDecimal amount,
                                                 String currency,
                                                 String receipt)
            throws RazorpayException {
        log.debug("Calling Razorpay to create order for receipt: {}", receipt);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",
                amount.multiply(BigDecimal.valueOf(100)).intValue());
        orderRequest.put("currency", currency);
        orderRequest.put("receipt",  receipt);
        orderRequest.put("payment_capture", 1);

        com.razorpay.Order order = razorpayClient.orders.create(orderRequest);
        return order.get("id");
    }

    public String razorpayOrderFallback(BigDecimal amount,
                                        String currency,
                                        String receipt,
                                        Throwable throwable) {
        log.error("Razorpay circuit breaker OPEN. Cause: {}",
                throwable.getMessage());
        throw new RuntimeException(
                "Payment gateway is currently unavailable. " +
                        "Please try again in a few moments.");
    }
}