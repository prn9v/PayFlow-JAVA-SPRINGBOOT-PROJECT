package com.pranav.notification_service.controller;

import com.pranav.notification_service.dto.request.SendNotificationRequest;
import com.pranav.notification_service.dto.response.NotificationResponse;
import com.pranav.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notification Management",
        description = "Manage, Search, Resend and Send Notifications"
)
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/{notificationId}
    @GetMapping("/{notificationId}")
    @Operation(
            summary = "Get Notification By ID",
            description = "Retrieves a notification using its unique notification ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class)
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
                    description = "Notification not found"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getById(notificationId));
    }

    // GET /api/notifications/reference/{referenceId}
    @Operation(
            summary = "Get Notifications By Reference ID",
            description = "Retrieves all notifications associated with a reference ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NotificationResponse.class)
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
            )
    })
    @GetMapping("/reference/{referenceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getByReference(
            @PathVariable String referenceId) {
        return ResponseEntity.ok(
                notificationService.getByReferenceId(referenceId));
    }

    // GET /api/notifications/customer/{email}
    @Operation(
            summary = "Get Notifications By Customer Email",
            description = "Retrieves all notifications sent to a customer email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NotificationResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email address"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @GetMapping("/customer/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getByCustomer(
            @PathVariable String email) {
        return ResponseEntity.ok(
                notificationService.getByCustomerEmail(email));
    }

    // POST /api/notifications/{notificationId}/resend
    @Operation(
            summary = "Resend Notification",
            description = "Resends an existing notification"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification resent successfully",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class)
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
                    description = "Notification not found"
            )
    })
    @PostMapping("/{notificationId}/resend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> resend(
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.resend(notificationId));
    }

    // POST /api/notifications/send
    @Operation(
            summary = "Send Manual Notification",
            description = "Manually sends a notification to a recipient"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification sent successfully",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid notification request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> sendManual(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendManual(request));
    }

    // GET /api/notifications/admin/all
    @Operation(
            summary = "Get All Notifications (Admin)",
            description = "Paginated list of all notifications with optional search and status filter"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications fetched successfully"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false)      String search,
            @RequestParam(required = false)      String status) {

        return ResponseEntity.ok(
                notificationService.getAllAdmin(page, size, sortBy, sortDir, search, status));
    }
}