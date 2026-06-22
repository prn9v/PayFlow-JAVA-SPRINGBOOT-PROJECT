package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.response.ApiCredentialResponse;
import com.pranav.merchant_service.service.ApiCredentialService;
import com.pranav.merchant_service.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants/api-credentials")
@RequiredArgsConstructor
@Tag(
        name = "Merchant API Credentials",
        description = "Generate, View, Rotate and Disable API Credentials for Merchants"
)
public class ApiCredentialController {

    private final ApiCredentialService apiCredentialService;
    private final MerchantService merchantService;

    // POST /api/merchants/api-credentials?merchantId={merchantId}  [ADMIN]
    @Operation(
            summary = "Generate API Credentials (Admin)",
            description = "Admin generates new API credentials for a specific merchant (pass merchantId as query param)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "API credentials generated successfully",
                    content = @Content(schema = @Schema(implementation = ApiCredentialResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found"),
            @ApiResponse(responseCode = "409", description = "Credentials already exist")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiCredentialResponse> generateCredentials(
            @RequestParam Long merchantId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiCredentialService.generateCredentials(merchantId));
    }

    // GET /api/merchants/api-credentials  [MERCHANT]
    @Operation(
            summary = "Get My API Credentials",
            description = "Retrieves all API credentials for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials fetched successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiCredentialResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<ApiCredentialResponse>> getCredentials(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(apiCredentialService.getCredentials(merchantId));
    }

    // PATCH /api/merchants/api-credentials/rotate  [MERCHANT]
    @Operation(
            summary = "Rotate My API Keys",
            description = "Rotates the API secret for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials rotated successfully",
                    content = @Content(schema = @Schema(implementation = ApiCredentialResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or credentials not found")
    })
    @PatchMapping("/rotate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiCredentialResponse> rotateKeys(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(apiCredentialService.rotateKeys(merchantId));
    }

    // PATCH /api/merchants/api-credentials/disable  [MERCHANT]
    @Operation(
            summary = "Disable My API Credentials",
            description = "Disables API credentials for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials disabled successfully",
                    content = @Content(schema = @Schema(implementation = ApiCredentialResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or credentials not found")
    })
    @PatchMapping("/disable")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiCredentialResponse> disableCredentials(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(apiCredentialService.disableCredentials(merchantId));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long extractUserId(Authentication authentication) {
        return Long.parseLong((String) authentication.getDetails());
    }

    private Long resolveMerchantId(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return merchantService.getMerchantByUserId(userId).getMerchantId();
    }
}