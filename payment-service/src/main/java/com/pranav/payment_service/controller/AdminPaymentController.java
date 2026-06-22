package com.pranav.payment_service.controller;

import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.exception.ForbiddenException;
import com.pranav.payment_service.exception.UnauthorizedException;
import com.pranav.payment_service.security.JwtService;
import com.pranav.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Admin - Payments", description = "Admin endpoints for all payments")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final JwtService     jwtService;

    // GET /api/payments/admin/all  [ADMIN]
    @Operation(
            summary = "Get All Payments (Admin)",
            description = "Paginated list of all payments across all merchants — admin only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied — not an admin")
    })
    @GetMapping("/admin/all")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "10")         int    size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir,
            @RequestParam(required = false)            String status,
            @RequestParam(required = false)            String search) {

        verifyAdminRole(request);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                paymentService.getAllPayments(pageable, status, search));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void verifyAdminRole(HttpServletRequest request) {
        String role = extractRoleFromCookie(request);
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Access denied — admin role required");
        }
    }

    private String extractRoleFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        Optional<String> token = Arrays.stream(request.getCookies())
                .filter(c -> "payflow_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();

        return token
                .map(jwtService::extractRole)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }
}