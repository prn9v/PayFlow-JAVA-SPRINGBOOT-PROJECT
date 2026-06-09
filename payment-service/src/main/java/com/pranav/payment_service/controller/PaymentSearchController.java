//package com.pranav.payment_service.controller;
//
//import com.pranav.payment_service.elasticsearch.PaymentDocument;
//import com.pranav.payment_service.service.PaymentIndexService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.ArraySchema;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/payments/search")
//@RequiredArgsConstructor
//@Tag(
//        name = "Payment Search",
//        description = "Search and filter indexed payment records using Elasticsearch"
//)
//public class PaymentSearchController {
//
//    private final PaymentIndexService paymentIndexService;
//
//    // GET /api/payments/search/merchant/{merchantId}
//    @Operation(
//            summary = "Search Payments By Merchant",
//            description = "Retrieves indexed payments belonging to a specific merchant"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Payments retrieved successfully",
//                    content = @Content(
//                            array = @ArraySchema(
//                                    schema = @Schema(implementation = PaymentDocument.class)
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "Merchant not found"
//            )
//    })
//    @GetMapping("/merchant/{merchantId}")
//    public ResponseEntity<List<PaymentDocument>> byMerchant(
//            @PathVariable Long merchantId) {
//        return ResponseEntity.ok(
//                paymentIndexService.searchByMerchantId(merchantId));
//    }
//
//    // GET /api/payments/search/customer?email=xxx
//    @Operation(
//            summary = "Search Payments By Customer Email",
//            description = "Retrieves indexed payments using customer email address"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Payments retrieved successfully",
//                    content = @Content(
//                            array = @ArraySchema(
//                                    schema = @Schema(implementation = PaymentDocument.class)
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "Invalid email parameter"
//            )
//    })
//    @GetMapping("/customer")
//    public ResponseEntity<List<PaymentDocument>> byCustomerEmail(
//            @RequestParam String email) {
//        return ResponseEntity.ok(
//                paymentIndexService.searchByCustomerEmail(email));
//    }
//
//    // GET /api/payments/search/status/{status}
//    @Operation(
//            summary = "Search Payments By Status",
//            description = "Retrieves indexed payments filtered by payment status"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Payments retrieved successfully",
//                    content = @Content(
//                            array = @ArraySchema(
//                                    schema = @Schema(implementation = PaymentDocument.class)
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "Invalid payment status"
//            )
//    })
//    @GetMapping("/status/{status}")
//    public ResponseEntity<List<PaymentDocument>> byStatus(
//            @PathVariable String status) {
//        return ResponseEntity.ok(
//                paymentIndexService.searchByStatus(status));
//    }
//
//
//    // GET /api/payments/search/reference/{reference}
//    @Operation(
//            summary = "Search Payments By Reference",
//            description = "Retrieves indexed payments using merchant reference value"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Payments retrieved successfully",
//                    content = @Content(
//                            array = @ArraySchema(
//                                    schema = @Schema(implementation = PaymentDocument.class)
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "No matching payments found"
//            )
//    })
//    @GetMapping("/reference/{reference}")
//    public ResponseEntity<List<PaymentDocument>> byReference(
//            @PathVariable String reference) {
//        return ResponseEntity.ok(
//                paymentIndexService.searchByReference(reference));
//    }
//}