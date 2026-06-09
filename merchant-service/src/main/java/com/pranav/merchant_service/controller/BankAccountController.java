package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.request.AddBankAccountRequest;
import com.pranav.merchant_service.dto.request.UpdateBankAccountRequest;
import com.pranav.merchant_service.dto.response.BankAccountResponse;
import com.pranav.merchant_service.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/merchants/{merchantId}/bank-accounts")
@RequiredArgsConstructor
@Tag(
        name = "Merchant Bank Accounts",
        description = "Manage merchant bank accounts including add, update, delete, view and set primary account"
)
public class BankAccountController {

    private final BankAccountService bankAccountService;

    // POST /api/merchants/{merchantId}/bank-accounts
    @Operation(
            summary = "Add Bank Account",
            description = "Adds a new bank account for a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank account added successfully",
                    content = @Content(
                            schema = @Schema(implementation = BankAccountResponse.class)
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bank account already exists"
            )
    })
    @PostMapping
    public ResponseEntity<BankAccountResponse> addBankAccount(
            @PathVariable Long merchantId,
            @Valid @RequestBody AddBankAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.addBankAccount(merchantId, request));
    }

    // GET /api/merchants/{merchantId}/bank-accounts
    @Operation(
            summary = "Get Merchant Bank Accounts",
            description = "Retrieves all bank accounts associated with a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank accounts fetched successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = BankAccountResponse.class)
                            )
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
    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getBankAccounts(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(bankAccountService.getBankAccounts(merchantId));
    }

    // GET /api/merchants/{merchantId}/bank-accounts/{bankAccountId}
    @Operation(
            summary = "Get Bank Account",
            description = "Retrieves a specific bank account by bank account ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = BankAccountResponse.class)
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
                    description = "Merchant or bank account not found"
            )
    })
    @GetMapping("/{bankAccountId}")
    public ResponseEntity<BankAccountResponse> getBankAccount(
            @PathVariable Long merchantId,
            @PathVariable Long bankAccountId) {
        return ResponseEntity.ok(bankAccountService.getBankAccount(merchantId, bankAccountId));
    }

    // PUT /api/merchants/{merchantId}/bank-accounts/{bankAccountId}
    @Operation(
            summary = "Update Bank Account",
            description = "Updates an existing merchant bank account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = BankAccountResponse.class)
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
                    description = "Merchant or bank account not found"
            )
    })
    @PutMapping("/{bankAccountId}")
    public ResponseEntity<BankAccountResponse> updateBankAccount(
            @PathVariable Long merchantId,
            @PathVariable Long bankAccountId,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        return ResponseEntity.ok(
                bankAccountService.updateBankAccount(merchantId, bankAccountId, request));
    }

    // DELETE /api/merchants/{merchantId}/bank-accounts/{bankAccountId}
    @Operation(
            summary = "Delete Bank Account",
            description = "Deletes a merchant bank account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Bank account deleted successfully"
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
                    description = "Merchant or bank account not found"
            )
    })
    @DeleteMapping("/{bankAccountId}")
    public ResponseEntity<Void> deleteBankAccount(
            @PathVariable Long merchantId,
            @PathVariable Long bankAccountId) {
        bankAccountService.deleteBankAccount(merchantId, bankAccountId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/merchants/{merchantId}/bank-accounts/{bankAccountId}/primary
    @Operation(
            summary = "Set Primary Bank Account",
            description = "Marks the selected bank account as the primary bank account for the merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Primary bank account updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = BankAccountResponse.class)
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
                    description = "Merchant or bank account not found"
            )
    })
    @PatchMapping("/{bankAccountId}/primary")
    public ResponseEntity<BankAccountResponse> setPrimary(
            @PathVariable Long merchantId,
            @PathVariable Long bankAccountId) {
        return ResponseEntity.ok(bankAccountService.setPrimary(merchantId, bankAccountId));
    }
}