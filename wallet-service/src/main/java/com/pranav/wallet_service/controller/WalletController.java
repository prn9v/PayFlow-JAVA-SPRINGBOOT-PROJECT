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

    private final WalletService     walletService;
    private final SettlementService settlementService;
    private final MerchantServiceClient merchantServiceClient;

    // GET /api/wallets/{merchantId}
    @Operation(
            summary = "Get Wallet",
            description = "Retrieves wallet details for a specific merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = WalletResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(
                walletService.getWalletByMerchantId(merchantId));
    }

    // GET /api/wallets/me
    @Operation(
            summary = "Get My Wallet",
            description = "Retrieves wallet details of the authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = WalletResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WalletResponse> getMyWallet(
            Authentication authentication) {
        Long userId =
                Long.parseLong((String) authentication.getDetails());

        MerchantResponse merchant =
                merchantServiceClient
                        .getMerchantByUserId(userId);

        log.info("merchant_id is: {}", merchant.getMerchantId());

        return ResponseEntity.ok(
                walletService.getWalletByMerchantId(
                        merchant.getMerchantId()));
    }

    // GET /api/wallets/{merchantId}/transactions
    @Operation(
            summary = "Get Wallet Transactions",
            description = "Retrieves all wallet transactions for a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = WalletTransactionResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{merchantId}/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(
                walletService.getTransactions(merchantId));
    }

    // GET /api/wallets/transactions/{transactionId}
    @Operation(
            summary = "Get Wallet Transaction",
            description = "Retrieves a wallet transaction by transaction ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = WalletTransactionResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/transactions/{transactionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<WalletTransactionResponse> getTransaction(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(
                walletService.getTransaction(transactionId));
    }

    // POST /api/wallets/{merchantId}/settlements
    @Operation(
            summary = "Request Settlement",
            description = "Creates a settlement request for a merchant wallet"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Settlement requested successfully",
                    content = @Content(
                            schema = @Schema(implementation = SettlementResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid settlement request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/{merchantId}/settlements")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<SettlementResponse> requestSettlement(
            @PathVariable Long merchantId,
            @Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.requestSettlement(
                        merchantId, request));
    }

    // GET /api/wallets/settlements/{settlementId}
    @Operation(
            summary = "Get Settlement",
            description = "Retrieves settlement details using settlement ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlement retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = SettlementResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    @GetMapping("/settlements/{settlementId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable Long settlementId) {
        return ResponseEntity.ok(
                settlementService.getSettlement(settlementId));
    }

    // GET /api/wallets/{merchantId}/settlements
    @Operation(
            summary = "Get Merchant Settlements",
            description = "Retrieves all settlements associated with a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlements retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = SettlementResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Merchant or wallet not found")
    })
    @GetMapping("/{merchantId}/settlements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<List<SettlementResponse>> getMerchantSettlements(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(
                settlementService.getMerchantSettlements(merchantId));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Long extractMerchantId(Authentication authentication) {
        // X-User-Id stored in details by GatewayAuthenticationFilter
        return Long.parseLong((String) authentication.getDetails());
    }
}