package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Admin - Payments", description = "Admin endpoints for all payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get all payments across all merchants (ADMIN only)")
    @GetMapping("/admin/all")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                paymentService.getAllPayments(pageable, status, search));
    }
}