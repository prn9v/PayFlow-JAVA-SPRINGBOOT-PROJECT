package com.pranav.payment_service.service;

import com.pranav.payment_service.dto.request.CreateRefundRequest;
import com.pranav.payment_service.dto.response.RefundResponse;
import com.pranav.payment_service.entity.Payment;
import com.pranav.payment_service.entity.Refund;
import com.pranav.payment_service.enums.PaymentStatus;
import com.pranav.payment_service.enums.RefundStatus;
import com.pranav.payment_service.exception.InvalidPaymentStateException;
import com.pranav.payment_service.exception.RefundNotFoundException;
import com.pranav.payment_service.rabbitmq.event.RefundCreatedEvent;
import com.pranav.payment_service.rabbitmq.producer.PaymentEventProducer;
import com.pranav.payment_service.repository.PaymentRepository;
import com.pranav.payment_service.repository.RefundRepository;
import com.pranav.payment_service.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository    refundRepository;
    private final PaymentService      paymentService;
    private final PaymentRepository   paymentRepository;
    private final PaymentEventProducer eventProducer;
    private final ReferenceGenerator  referenceGenerator;

    // ─── Create Refund ────────────────────────────────────────────────────────

    @Transactional
    public RefundResponse createRefund(UUID paymentId,
                                       CreateRefundRequest request) {
        Payment payment = paymentService.findById(paymentId);

        // Only SUCCESS payments can be refunded
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Only successful payments can be refunded. Current: "
                            + payment.getStatus());
        }

        // Refund amount cannot exceed payment amount
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentStateException(
                    "Refund amount cannot exceed payment amount of "
                            + payment.getAmount());
        }

        Refund refund = Refund.builder()
                .paymentId(paymentId)
                .merchantId(payment.getMerchantId())
                .refundReference(referenceGenerator.generateRefundReference())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .build();

        refundRepository.save(refund);

        // Update payment status to REFUNDED
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Publish Kafka event
        eventProducer.publishRefundCreated(
                RefundCreatedEvent.builder()
                        .refundId(refund.getId())
                        .paymentId(paymentId)
                        .merchantId(payment.getMerchantId())
                        .refundReference(refund.getRefundReference())
                        .amount(refund.getAmount())
                        .reason(refund.getReason())
                        .customerEmail(payment.getCustomerEmail())
                        .build()
        );

        log.info("Refund created: {} for payment: {}",
                refund.getRefundReference(), paymentId);

        return toResponse(refund);
    }

    public List<RefundResponse> getMerchantRefunds(Long merchantId) {

        return refundRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Get Refund ───────────────────────────────────────────────────────────

    public RefundResponse getRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(
                        "Refund not found: " + refundId));
        return toResponse(refund);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private RefundResponse toResponse(Refund r) {
        return RefundResponse.builder()
                .refundId(r.getId())
                .paymentId(r.getPaymentId())
                .merchantId(r.getMerchantId())
                .refundReference(r.getRefundReference())
                .amount(r.getAmount())
                .reason(r.getReason())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }
}