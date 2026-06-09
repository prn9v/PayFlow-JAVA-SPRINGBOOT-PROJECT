package com.pranav.wallet_service.controller;

import com.pranav.wallet_service.dto.request.CreditDebitRequest;
import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/internal/wallets")
@RequiredArgsConstructor
@Tag(
        name = "Internal Wallet APIs",
        description = "Internal service-to-service wallet operations"
)
public class InternalWalletController {

    private final WalletService walletService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // POST /internal/wallets/credit
    @Operation(
            summary = "Credit Wallet",
            description = "Internal API used to credit a merchant wallet after successful payment"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet credited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid internal API key"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/credit")
    public ResponseEntity<?> credit(
            @Valid @RequestBody CreditDebitRequest request,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        walletService.credit(
                request.getMerchantId(),
                request.getAmount(),
                request.getReferenceId(),
                ReferenceType.PAYMENT,
                request.getDescription()
        );

        return ResponseEntity.ok(Map.of("message", "Wallet credited successfully"));
    }



    // POST /internal/wallets/debit
    @Operation(
            summary = "Debit Wallet",
            description = "Internal API used to debit a merchant wallet during refund processing"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet debited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid internal API key"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/debit")
    public ResponseEntity<?> debit(
            @Valid @RequestBody CreditDebitRequest request,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        walletService.debit(
                request.getMerchantId(),
                request.getAmount(),
                request.getReferenceId(),
                ReferenceType.REFUND,
                request.getDescription()
        );

        return ResponseEntity.ok(Map.of("message", "Wallet debited successfully"));
    }



    @Operation(
            summary = "Create Wallet For Merchant",
            description = "Internal API used to create a wallet for a newly onboarded merchant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid internal API key"),
            @ApiResponse(responseCode = "404", description = "Merchant not found"),
            @ApiResponse(responseCode = "409", description = "Wallet already exists")
    })
    @PostMapping("/create-for-merchant/{merchantId}")
    public ResponseEntity<?> createWalletForMerchant(
            @PathVariable Long merchantId,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        walletService.createWallet(merchantId);
        return ResponseEntity.ok(
                Map.of("message", "Wallet created for merchant: " + merchantId));
    }
}