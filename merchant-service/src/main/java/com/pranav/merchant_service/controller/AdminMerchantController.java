package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.response.MerchantResponse;
import com.pranav.merchant_service.service.MerchantService;
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
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Tag(name = "Admin - Merchants", description = "Admin endpoints for merchant management")
public class AdminMerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "Get all merchants with pagination (ADMIN only)")
    @GetMapping
    public ResponseEntity<Page<MerchantResponse>> getAllMerchants(
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

        Page<MerchantResponse> merchants =
                merchantService.getAllMerchants(pageable, status, search);

        return ResponseEntity.ok(merchants);
    }
}