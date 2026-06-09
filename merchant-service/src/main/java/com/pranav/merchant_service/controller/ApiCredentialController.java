package com.pranav.merchant_service.controller;

import com.pranav.merchant_service.dto.response.ApiCredentialResponse;
import com.pranav.merchant_service.service.ApiCredentialService;
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
@RequestMapping("/api/merchants/{merchantId}/api-credentials")
@RequiredArgsConstructor
@Tag(
        name = "Merchant API Credentials",
        description = "Generate, View, Rotate and Disable API Credentials for Merchants"
)
public class ApiCredentialController {

    private final ApiCredentialService apiCredentialService;

    // POST /api/merchants/{merchantId}/api-credentials
    @Operation(
            summary = "Generate API Credentials",
            description = "Generates new API credentials including API key and secret for a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "API credentials generated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ApiCredentialResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
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
                    description = "Credentials already exist"
            )
    })
    @PostMapping
    public ResponseEntity<ApiCredentialResponse> generateCredentials(
            @PathVariable Long merchantId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiCredentialService.generateCredentials(merchantId));
    }

    // GET /api/merchants/{merchantId}/api-credentials
    @Operation(
            summary = "Get API Credentials",
            description = "Retrieves all API credentials associated with a merchant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials fetched successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ApiCredentialResponse.class)
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
    public ResponseEntity<List<ApiCredentialResponse>> getCredentials(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(apiCredentialService.getCredentials(merchantId));
    }

    // PATCH /api/merchants/{merchantId}/api-credentials/rotate
    @Operation(
            summary = "Rotate API Keys",
            description = "Generates a new API secret and rotates existing API credentials"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials rotated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ApiCredentialResponse.class)
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
                    description = "Merchant or credentials not found"
            )
    })
    @PatchMapping("/rotate")
    public ResponseEntity<ApiCredentialResponse> rotateKeys(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(apiCredentialService.rotateKeys(merchantId));
    }

    // PATCH /api/merchants/{merchantId}/api-credentials/disable
    @Operation(
            summary = "Disable API Credentials",
            description = "Disables merchant API credentials and prevents further API access"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API credentials disabled successfully",
                    content = @Content(
                            schema = @Schema(implementation = ApiCredentialResponse.class)
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
                    description = "Merchant or credentials not found"
            )
    })
    @PatchMapping("/disable")
    public ResponseEntity<ApiCredentialResponse> disableCredentials(
            @PathVariable Long merchantId) {
        return ResponseEntity.ok(apiCredentialService.disableCredentials(merchantId));
    }
}