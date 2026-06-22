package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.request.CreatePaymentRequest;
import com.pranav.payment_service.dto.response.CreatePaymentResponse;
import com.pranav.payment_service.dto.response.MerchantResponse;
import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.service.PaymentService;
import com.pranav.payment_service.client.MerchantServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(
        name = "Payment Management",
        description = "Create, Retrieve, Manage and Process Payments"
)
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantServiceClient merchantServiceClient;

    // POST /api/payments
    @Operation(
            summary = "Create Payment",
            description = "Creates a new payment and returns payment details along with gateway information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    // GET /api/payments/{paymentId}
    @Operation(
            summary = "Get Payment By ID",
            description = "Retrieves payment details using payment ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payment ID"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    // GET /api/payments/merchant  [MERCHANT — resolved from auth]
    @Operation(
            summary = "Get My Payments",
            description = "Retrieves all payments for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized — merchant not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    @GetMapping("/merchant")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(paymentService.getMerchantPayments(merchantId));
    }

    // GET /api/payments/merchant/{merchantId}  [ADMIN]
    @Operation(
            summary = "Get Merchant Payments (Admin)",
            description = "Admin retrieves all payments for a specific merchant by merchant ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getMerchantPayments(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(paymentService.getMerchantPayments(merchantId));
    }

    // PATCH /api/payments/{paymentId}/cancel
    @Operation(
            summary = "Cancel Payment",
            description = "Cancels a payment if it is still eligible for cancellation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId));
    }

    // POST /api/payments/webhook  [PUBLIC — called by Razorpay, no JWT]
    @Operation(
            summary = "Razorpay Webhook",
            description = "Receives webhook notifications from Razorpay and updates payment status accordingly"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload or signature"),
            @ApiResponse(responseCode = "401", description = "Unauthorized webhook request")
    })
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long extractUserId(Authentication authentication) {
        // userId stored as String in details by GatewayAuthenticationFilter
        return Long.parseLong((String) authentication.getDetails());
    }

    private Long resolveMerchantId(Authentication authentication) {
        Long userId = extractUserId(authentication);
        MerchantResponse merchant = merchantServiceClient.getMerchantByUserId(userId);
        return merchant.getMerchantId();
    }
}