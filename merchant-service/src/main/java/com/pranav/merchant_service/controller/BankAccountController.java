package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.request.AddBankAccountRequest;
import com.pranav.merchant_service.dto.request.UpdateBankAccountRequest;
import com.pranav.merchant_service.dto.response.BankAccountResponse;
import com.pranav.merchant_service.service.BankAccountService;
import com.pranav.merchant_service.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants/bank-accounts")
@RequiredArgsConstructor
@Tag(
        name = "Merchant Bank Accounts",
        description = "Manage merchant bank accounts including add, update, delete, view and set primary account"
)
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final MerchantService merchantService;

    // POST /api/merchants/bank-accounts  [MERCHANT]
    @Operation(
            summary = "Add Bank Account",
            description = "Adds a new bank account for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank account added successfully",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found"),
            @ApiResponse(responseCode = "409", description = "Bank account already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<BankAccountResponse> addBankAccount(
            Authentication authentication,
            @Valid @RequestBody AddBankAccountRequest request) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.addBankAccount(merchantId, request));
    }

    // GET /api/merchants/bank-accounts  [MERCHANT]
    @Operation(
            summary = "Get My Bank Accounts",
            description = "Retrieves all bank accounts for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank accounts fetched successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BankAccountResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<BankAccountResponse>> getBankAccounts(
            Authentication authentication) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(bankAccountService.getBankAccounts(merchantId));
    }

    // GET /api/merchants/bank-accounts/{bankAccountId}  [MERCHANT]
    @Operation(
            summary = "Get My Bank Account",
            description = "Retrieves a specific bank account of the currently authenticated merchant by bank account ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account fetched successfully",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or bank account not found")
    })
    @GetMapping("/{bankAccountId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<BankAccountResponse> getBankAccount(
            Authentication authentication,
            @PathVariable Long bankAccountId) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(bankAccountService.getBankAccount(merchantId, bankAccountId));
    }

    // PUT /api/merchants/bank-accounts/{bankAccountId}  [MERCHANT]
    @Operation(
            summary = "Update Bank Account",
            description = "Updates an existing bank account of the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account updated successfully",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or bank account not found")
    })
    @PutMapping("/{bankAccountId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<BankAccountResponse> updateBankAccount(
            Authentication authentication,
            @PathVariable Long bankAccountId,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(
                bankAccountService.updateBankAccount(merchantId, bankAccountId, request));
    }

    // DELETE /api/merchants/bank-accounts/{bankAccountId}  [MERCHANT]
    @Operation(
            summary = "Delete Bank Account",
            description = "Deletes a bank account of the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or bank account not found")
    })
    @DeleteMapping("/{bankAccountId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteBankAccount(
            Authentication authentication,
            @PathVariable Long bankAccountId) {
        Long merchantId = resolveMerchantId(authentication);
        bankAccountService.deleteBankAccount(merchantId, bankAccountId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/merchants/bank-accounts/{bankAccountId}/primary  [MERCHANT]
    @Operation(
            summary = "Set Primary Bank Account",
            description = "Marks a bank account as primary for the currently authenticated merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Primary bank account updated successfully",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or bank account not found")
    })
    @PatchMapping("/{bankAccountId}/primary")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<BankAccountResponse> setPrimary(
            Authentication authentication,
            @PathVariable Long bankAccountId) {
        Long merchantId = resolveMerchantId(authentication);
        return ResponseEntity.ok(bankAccountService.setPrimary(merchantId, bankAccountId));
    }

    // GET /api/merchants/bank-accounts/admin/{bankAccountId}?merchantId={merchantId}  [ADMIN]
    @Operation(
            summary = "Get Bank Account (Admin)",
            description = "Admin retrieves a specific bank account by bank account ID for any merchant (pass merchantId as query param)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account fetched successfully",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Merchant or bank account not found")
    })
    @GetMapping("/admin/{bankAccountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountResponse> getBankAccountAdmin(
            @RequestParam Long merchantId,
            @PathVariable Long bankAccountId) {
        return ResponseEntity.ok(bankAccountService.getBankAccount(merchantId, bankAccountId));
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