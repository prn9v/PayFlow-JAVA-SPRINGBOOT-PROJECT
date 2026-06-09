package com.pranav.payment_service.controller;

import com.pranav.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
@Tag(
        name = "Internal Payment APIs",
        description = "Internal service-to-service payment validation APIs"
)
public class InternalPaymentController {

    private final PaymentService paymentService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // GET /internal/payments/{paymentId}/validate
    @Operation(
            summary = "Validate Payment",
            description = "Internal API used by other microservices to verify whether a payment exists and is valid"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment validation completed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid internal API key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    @GetMapping("/{paymentId}/validate")
    public ResponseEntity<?> validatePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        boolean valid = paymentService.validatePayment(paymentId);
        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "valid", valid));
    }
}