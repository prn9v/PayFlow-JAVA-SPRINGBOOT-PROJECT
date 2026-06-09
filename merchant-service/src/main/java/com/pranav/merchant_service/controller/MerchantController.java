package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.request.CreateMerchantRequest;
import com.pranav.merchant_service.dto.request.UpdateMerchantRequest;
import com.pranav.merchant_service.dto.response.MerchantResponse;
import com.pranav.merchant_service.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Tag(
        name = "Merchant Management",
        description = "Merchant Profile Management and Merchant Status Operations"
)
public class MerchantController {

    private final MerchantService merchantService;

    // ─── Module 1: Merchant Profile ──────────────────────────────────────────

    // POST /api/merchants
    @Operation(
            summary = "Create Merchant",
            description = "Creates a new merchant profile for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Merchant created successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Merchant already exists for this user"
            )
    })
    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(
            @Valid @RequestBody CreateMerchantRequest request,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.createMerchant(request, userId));
    }

    // GET /api/merchants/{merchantId}
    @Operation(
            summary = "Get Merchant By ID",
            description = "Fetch merchant details using merchant ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
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
                    description = "Merchant not found"
            )
    })
    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<MerchantResponse> getMerchantById(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.getMerchantById(merchantId));
    }

    // GET /api/merchants/me
    @Operation(
            summary = "Get My Merchant Profile",
            description = "Fetch merchant profile of currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant profile fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
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
                    description = "Merchant profile not found"
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<MerchantResponse> getMyProfile(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(merchantService.getMerchantByUserId(userId));
    }

    // PUT /api/merchants/{merchantId}
    @Operation(
            summary = "Update Merchant",
            description = "Updates merchant profile information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
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
                    description = "Merchant not found"
            )
    })
    @PutMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @PathVariable Long merchantId,
            @Valid @RequestBody UpdateMerchantRequest request) {
        return ResponseEntity.ok(merchantService.updateMerchant(merchantId, request));
    }

    // DELETE /api/merchants/{merchantId}
    @Operation(
            summary = "Delete Merchant",
            description = "Deletes a merchant profile permanently"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Merchant deleted successfully"
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
                    description = "Merchant not found"
            )
    })
    @DeleteMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long merchantId) {
        merchantService.deleteMerchant(merchantId);
        return ResponseEntity.noContent().build();
    }

    // ─── Module 2: Merchant Status ────────────────────────────────────────────

    // PATCH /api/merchants/{merchantId}/activate
    @Operation(
            summary = "Activate Merchant",
            description = "Activates a merchant account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant activated successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
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
                    description = "Merchant not found"
            )
    })
    @PatchMapping("/{merchantId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantResponse> activate(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.activateMerchant(merchantId));
    }

    // PATCH /api/merchants/{merchantId}/suspend
    @Operation(
            summary = "Suspend Merchant",
            description = "Suspends a merchant account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant suspended successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
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
                    description = "Merchant not found"
            )
    })
    @PatchMapping("/{merchantId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantResponse> suspend(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.suspendMerchant(merchantId));
    }

    // PATCH /api/merchants/{merchantId}/reject
    @Operation(
            summary = "Reject Merchant",
            description = "Rejects a merchant account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant rejected successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
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
                    description = "Merchant not found"
            )
    })
    @PatchMapping("/{merchantId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantResponse> reject(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.rejectMerchant(merchantId));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Long extractUserId(Authentication authentication) {
        // userId was stored in details by JwtAuthenticationFilter
        return Long.parseLong((String) authentication.getDetails());
    }
}