package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.response.CreatePaymentResponse;
import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.exception.UnauthorizedException;
import com.pranav.payment_service.security.JwtService;
import com.pranav.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Customer - Payments", description = "Customer payment endpoints")
public class CustomerPaymentController {

    private final PaymentService paymentService;
    private final JwtService     jwtService;

    // GET /api/payments/customer
    @Operation(
            summary = "Get My Payments",
            description = "Retrieves all payments for the currently authenticated customer"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized — not authenticated")
    })
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponse>> getCustomerPayments(
            HttpServletRequest request) {
        String email = extractEmailFromCookie(request);
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerEmail(email));
    }

    // POST /api/payments/initiate/{paymentId}
    @Operation(
            summary = "Initiate Payment",
            description = "Returns Razorpay order details for frontend checkout for a pending payment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized — not authenticated"),
            @ApiResponse(responseCode = "400", description = "Payment not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/initiate/{paymentId}")
    public ResponseEntity<CreatePaymentResponse> initiatePayment(
            @PathVariable UUID paymentId,
            HttpServletRequest request) {
        String email = extractEmailFromCookie(request);
        return ResponseEntity.ok(
                paymentService.initiateExistingPayment(paymentId, email));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String extractEmailFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        Optional<String> token = Arrays.stream(request.getCookies())
                .filter(c -> "payflow_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();

        return token
                .map(jwtService::extractEmail)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }
}