package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.request.CreateRefundRequest;
import com.pranav.payment_service.dto.response.RefundResponse;
import com.pranav.payment_service.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Refund Management",
        description = "Create and Retrieve Payment Refunds"
)
public class RefundController {

    private final RefundService refundService;

    // POST /api/payments/{paymentId}/refund
    @Operation(
            summary = "Create Refund",
            description = "Creates a full or partial refund for an existing payment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Refund created successfully",
                    content = @Content(
                            schema = @Schema(implementation = RefundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refund request or refund amount exceeds payment amount"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Refund cannot be processed for the current payment state"
            )
    })
    @PostMapping("/api/payments/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<RefundResponse> createRefund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody CreateRefundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.createRefund(paymentId, request));
    }

    // GET /api/refunds/{refundId}
    @Operation(
            summary = "Get Refund By ID",
            description = "Retrieves refund details using refund ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Refund retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RefundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Refund not found"
            )
    })
    @GetMapping("/api/refunds/{refundId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<RefundResponse> getRefund(
            @PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefund(refundId));
    }
}