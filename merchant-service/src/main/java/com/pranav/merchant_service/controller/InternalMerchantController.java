package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.request.ValidateCredentialRequest;
import com.pranav.merchant_service.dto.response.MerchantResponse;
import com.pranav.merchant_service.service.ApiCredentialService;
import com.pranav.merchant_service.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/internal/merchants")
@RequiredArgsConstructor
@Tag(
        name = "Internal Merchant APIs",
        description = "Internal service-to-service APIs for merchant validation, credential validation and merchant lookup"
)
public class InternalMerchantController {

    private final MerchantService merchantService;
    private final ApiCredentialService apiCredentialService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // GET /internal/merchants/{merchantId}/validate
    @Operation(
            summary = "Validate Merchant",
            description = "Internal API used by other microservices to verify whether a merchant exists and is active"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant validation completed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid internal API key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant not found"
            )
    })
    @GetMapping("/{merchantId}/validate")
    public ResponseEntity<?> validateMerchant(
            @PathVariable Long merchantId,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized internal request"));
        }

        boolean isActive = merchantService.validateMerchant(merchantId);
        return ResponseEntity.ok(Map.of(
                "merchantId", merchantId,
                "active", isActive));
    }

    @Operation(
            summary = "Validate Merchant",
            description = "Internal API used by other microservices to verify whether a merchant exists and is active"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant validation completed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid internal API key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant not found"
            )
    })
    @PostMapping("/credentials/validate")
    public ResponseEntity<?> validateCredentials(
            @RequestBody ValidateCredentialRequest request,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        return ResponseEntity.ok(
                apiCredentialService.validateCredentials(
                        request.getPublicKey(),
                        request.getSecretKey()));
    }

    // GET /internal/merchants/user/{userId}
    @Operation(
            summary = "Get Merchant By User ID",
            description = "Internal API used to retrieve merchant details associated with a specific user ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = MerchantResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid internal API key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant not found"
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getMerchantByUserId(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized internal request"));
        }

        MerchantResponse response = merchantService.getMerchantByUserId(userId);
        return ResponseEntity.ok(response);
    }
}