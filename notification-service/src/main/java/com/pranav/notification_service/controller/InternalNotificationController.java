package com.pranav.notification_service.controller;

import com.pranav.notification_service.dto.request.SendNotificationRequest;
import com.pranav.notification_service.service.NotificationService;
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
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Internal Notification APIs",
        description = "Internal service-to-service notification APIs"
)
public class InternalNotificationController {

    private final NotificationService notificationService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // POST /internal/notifications/email
    @Operation(
            summary = "Send Internal Email",
            description = "Internal API used by other microservices to send email notifications"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email queued successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid notification request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid internal API key"
            )
    })
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(
            @Valid @RequestBody SendNotificationRequest request,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        notificationService.sendInternal(
                request.getRecipientEmail(),
                request.getSubject(),
                request.getMessage()
        );

        return ResponseEntity.ok(
                Map.of("message", "Email queued successfully"));
    }
}