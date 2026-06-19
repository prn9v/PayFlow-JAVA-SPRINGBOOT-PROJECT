package com.pranav.notification_service.service;

import com.pranav.notification_service.dto.request.SendNotificationRequest;
import com.pranav.notification_service.dto.response.NotificationResponse;
import com.pranav.notification_service.email.EmailService;
import com.pranav.notification_service.entity.Notification;
import com.pranav.notification_service.entity.NotificationTemplate;
import com.pranav.notification_service.enums.NotificationStatus;
import com.pranav.notification_service.enums.NotificationType;
import com.pranav.notification_service.enums.ReferenceType;
import com.pranav.notification_service.exception.NotificationNotFoundException;
import com.pranav.notification_service.exception.TemplateNotFoundException;
import com.pranav.notification_service.rabbitmq.event.*;
import com.pranav.notification_service.repository.NotificationRepository;
import com.pranav.notification_service.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository         notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService                   emailService;

    // ─── Core: Build from template and send ──────────────────────────────────

    @Transactional
    public void sendFromTemplate(String templateCode,
                                 String recipientEmail,
                                 String referenceId,
                                 ReferenceType referenceType,
                                 Map<String, String> placeholders) {

        // 1. Load template from DB
        NotificationTemplate template = templateRepository
                .findByTemplateCodeAndActiveTrue(templateCode)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template not found: " + templateCode));

        // 2. Replace placeholders in subject and body
        String subject = resolvePlaceholders(template.getSubject(), placeholders);
        String body    = resolvePlaceholders(template.getBody(),    placeholders);

        // 3. Save notification record as PENDING
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .recipientEmail(recipientEmail)
                        .subject(subject)
                        .message(body)
                        .notificationType(NotificationType.EMAIL)
                        .status(NotificationStatus.PENDING)
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .build()
        );

        // 4. Attempt to send email
        sendAndUpdateStatus(notification, recipientEmail, subject, body);
    }

    // ─── Manual send (admin API) ──────────────────────────────────────────────

    @Transactional
    public NotificationResponse sendManual(SendNotificationRequest request) {
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .recipientEmail(request.getRecipientEmail())
                        .subject(request.getSubject())
                        .message(request.getMessage())
                        .notificationType(NotificationType.EMAIL)
                        .status(NotificationStatus.PENDING)
                        .build()
        );

        sendAndUpdateStatus(
                notification,
                request.getRecipientEmail(),
                request.getSubject(),
                request.getMessage()
        );

        return toResponse(notificationRepository.save(notification));
    }

    // ─── Resend failed notification ───────────────────────────────────────────

    @Transactional
    public NotificationResponse resend(Long notificationId) {
        Notification notification = findById(notificationId);

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException(
                    "Only FAILED notifications can be resent. Current status: "
                            + notification.getStatus());
        }

        // Reset to pending before retry
        notification.setStatus(NotificationStatus.PENDING);
        notification.setFailureReason(null);
        notificationRepository.save(notification);

        sendAndUpdateStatus(
                notification,
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getMessage()
        );

        return toResponse(notificationRepository.save(notification));
    }

    // ─── Read APIs ────────────────────────────────────────────────────────────

    public NotificationResponse getById(Long notificationId) {
        return toResponse(findById(notificationId));
    }

    public List<NotificationResponse> getByReferenceId(String referenceId) {
        return notificationRepository.findByReferenceId(referenceId)
                .stream().map(this::toResponse).toList();
    }

    public List<NotificationResponse> getByCustomerEmail(String email) {
        return notificationRepository.findByRecipientEmail(email)
                .stream().map(this::toResponse).toList();
    }

    // ─── Internal send (called by other services) ─────────────────────────────

    @Transactional
    public void sendInternal(String recipientEmail,
                             String subject,
                             String message) {
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .recipientEmail(recipientEmail)
                        .subject(subject)
                        .message(message)
                        .notificationType(NotificationType.EMAIL)
                        .status(NotificationStatus.PENDING)
                        .build()
        );
        sendAndUpdateStatus(notification, recipientEmail, subject, message);
    }

    // ─── RabbitMQ Event Handlers ──────────────────────────────────────────────────

    public void sendPaymentCreatedEmail(PaymentCreatedEvent event) {
        sendFromTemplate(
                "PAYMENT_CREATED",
                event.getCustomerEmail(),
                event.getPaymentId().toString(),
                ReferenceType.PAYMENT,
                Map.of(
                        "customerName",      event.getCustomerName(),
                        "paymentReference",  event.getPaymentReference(),
                        "amount",            String.valueOf(event.getAmount()),
                        "currency",          event.getCurrency()
                )
        );
    }

    public void sendPaymentSuccessEmail(PaymentSuccessEvent event) {
        sendFromTemplate(
                "PAYMENT_SUCCESS",
                event.getCustomerEmail(),
                event.getPaymentId().toString(),
                ReferenceType.PAYMENT,
                Map.of(
                        "paymentReference",  event.getPaymentReference(),
                        "amount",            String.valueOf(event.getAmount()),
                        "currency",          event.getCurrency()
                )
        );
    }

    public void sendPaymentFailedEmail(PaymentFailedEvent event) {
        sendFromTemplate(
                "PAYMENT_FAILED",
                event.getCustomerEmail(),
                event.getPaymentId().toString(),
                ReferenceType.PAYMENT,
                Map.of(
                        "paymentReference",  event.getPaymentReference(),
                        "amount",            String.valueOf(event.getAmount()),
                        "reason",            event.getReason() != null
                                ? event.getReason() : "Unknown"
                )
        );
    }

    public void sendRefundCreatedEmail(RefundCreatedEvent event) {
        sendFromTemplate(
                "REFUND_CREATED",
                event.getCustomerEmail(),
                event.getRefundId().toString(),
                ReferenceType.REFUND,
                Map.of(
                        "paymentReference",  event.getRefundReference(),
                        "amount",            String.valueOf(event.getAmount()),
                        "reason",            event.getReason() != null
                                ? event.getReason() : "No reason provided"
                )
        );
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void sendAndUpdateStatus(Notification notification,
                                     String toEmail,
                                     String subject,
                                     String body) {
        try {
            emailService.sendEmail(toEmail, subject, body);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
        }
        notificationRepository.save(notification);
    }

    private String resolvePlaceholders(String template,
                                       Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return template;
        String resolved = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace(
                    "{{" + entry.getKey() + "}}", entry.getValue());
        }
        return resolved;
    }

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + id));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientEmail(n.getRecipientEmail())
                .subject(n.getSubject())
                .status(n.getStatus().name())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType() != null
                        ? n.getReferenceType().name() : null)
                .failureReason(n.getFailureReason())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}