package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.response.CreatePaymentResponse;
import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Customer - Payments", description = "Customer payment endpoints")
public class CustomerPaymentController {

    private final PaymentService paymentService;

    // ─── Customer sees all their own payments ─────────────────────────────────

    @Operation(
            summary = "Get all payments for logged-in customer",
            description = "Uses X-User-Email header to find all payments for customer"
    )
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponse>> getCustomerPayments(
            @RequestHeader("X-User-Email") String customerEmail) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByCustomerEmail(customerEmail));
    }

    // ─── Customer initiates payment for a pending order ───────────────────────

    @Operation(
            summary = "Initiate Razorpay payment for a pending payment",
            description = "Returns Razorpay order details for frontend checkout"
    )
    @PostMapping("/initiate/{paymentId}")
    public ResponseEntity<CreatePaymentResponse> initiatePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Email") String customerEmail) {

        return ResponseEntity.ok(
                paymentService.initiateExistingPayment(
                        paymentId, customerEmail));
    }
}