package com.pranav.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_templates",
        indexes = {
                @Index(name = "idx_template_code", columnList = "template_code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. PAYMENT_SUCCESS, PAYMENT_FAILED, REFUND_CREATED, PAYMENT_CREATED
    @Column(name = "template_code", nullable = false, unique = true)
    private String templateCode;

    @Column(name = "subject", nullable = false)
    private String subject;

    // Supports placeholders: {{customerName}}, {{amount}}, {{currency}}, etc.
    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }
}