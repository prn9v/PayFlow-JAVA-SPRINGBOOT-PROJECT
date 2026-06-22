package com.pranav.wallet_service.controller;

import com.pranav.wallet_service.client.MerchantServiceClient;
import com.pranav.wallet_service.dto.request.SettlementRequest;
import com.pranav.wallet_service.dto.response.MerchantResponse;
import com.pranav.wallet_service.dto.response.SettlementResponse;
import com.pranav.wallet_service.dto.response.WalletResponse;
import com.pranav.wallet_service.dto.response.WalletTransactionResponse;
import com.pranav.wallet_service.service.SettlementService;
import com.pranav.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(
        name = "Wallet Management",
        description = "Manage Merchant Wallets, Transactions and Settlements"
)
public class WalletController {

    private final WalletService         walletService;
    private final SettlementService     settlementService;
    private final MerchantServiceClient merchantServiceClient;

    // ─── MERCHANT ROUTES (merchantId resolved from authenticated user) ────────

    // GET /api/wallets/me  [MERCHANT]
    @Operation(
            summary = "Get My Wallet",
            description = "Retrieves wallet of the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WalletResponse> getMyWallet(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(walletService.getWalletByMerchantId(merchantId));
    }

    // GET /api/wallets/transactions  [MERCHANT]
    @Operation(
            summary = "Get My Wallet Transactions",
            description = "Retrieves all wallet transactions of the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = WalletTransactionResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<WalletTransactionResponse>> getMyTransactions(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(walletService.getTransactions(merchantId));
    }

    // GET /api/wallets/settlements  [MERCHANT]
    @Operation(
            summary = "Get My Settlements",
            description = "Retrieves all settlements of the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlements retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SettlementResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/settlements")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<SettlementResponse>> getMySettlements(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(settlementService.getMerchantSettlements(merchantId));
    }

    // POST /api/wallets/settlements  [MERCHANT]
    @Operation(
            summary = "Request Settlement",
            description = "Creates a settlement request for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Settlement requested successfully",
                    content = @Content(schema = @Schema(implementation = SettlementResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid settlement request or insufficient balance"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/settlements")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<SettlementResponse> requestSettlement(
            Authentication authentication,
            @Valid @RequestBody SettlementRequest request) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.requestSettlement(merchantId, request));
    }

    // GET /api/wallets/settlements/{settlementId}  [MERCHANT or ADMIN]
    @Operation(
            summary = "Get Settlement By ID",
            description = "Retrieves a specific settlement by settlement ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlement retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SettlementResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    @GetMapping("/settlements/{settlementId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.getSettlement(settlementId));
    }

    // ─── ADMIN ROUTES (merchantId as path variable) ───────────────────────────

    // GET /api/wallets/{merchantId}  [ADMIN]
    @Operation(
            summary = "Get Merchant Wallet (Admin)",
            description = "Admin retrieves wallet of a specific merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(walletService.getWalletByMerchantId(merchantId));
    }

    // GET /api/wallets/{merchantId}/transactions  [ADMIN]
    @Operation(
            summary = "Get Merchant Transactions (Admin)",
            description = "Admin retrieves all wallet transactions for a specific merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = WalletTransactionResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{merchantId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(walletService.getTransactions(merchantId));
    }

    // GET /api/wallets/{merchantId}/settlements  [ADMIN]
    @Operation(
            summary = "Get Merchant Settlements (Admin)",
            description = "Admin retrieves all settlements for a specific merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlements retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SettlementResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or wallet not found")
    })
    @GetMapping("/{merchantId}/settlements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SettlementResponse>> getMerchantSettlements(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(settlementService.getMerchantSettlements(merchantId));
    }

    // POST /api/wallets/{merchantId}/settlements  [ADMIN]
    @Operation(
            summary = "Request Settlement for Merchant (Admin)",
            description = "Admin creates a settlement request on behalf of a specific merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Settlement requested successfully",
                    content = @Content(schema = @Schema(implementation = SettlementResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid settlement request or insufficient balance"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/{merchantId}/settlements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettlementResponse> requestSettlementAdmin(
            @PathVariable Long merchantId,
            @Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.requestSettlement(merchantId, request));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long extractUserId(Authentication authentication) {
        // userId stored as String in details by GatewayAuthenticationFilter
        return Long.parseLong((String) authentication.getDetails());
    }

    private Long resolveMerchantId(Authentication authentication) {
        Long userId = extractUserId(authentication);
        MerchantResponse merchant = merchantServiceClient.getMerchantByUserId(userId);
        log.info("Resolved merchantId: {} for userId: {}", merchant.getMerchantId(), userId);
        return merchant.getMerchantId();
    }
}