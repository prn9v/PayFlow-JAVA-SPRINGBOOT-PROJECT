// NotificationResponse.java
package com.pranav.notification_service.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String status;
    private String referenceId;
    private String referenceType;
    private String failureReason;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}