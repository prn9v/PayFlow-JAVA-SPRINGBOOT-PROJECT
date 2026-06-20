package com.pranav.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranav.payment_service.client.MerchantServiceClient;
import com.pranav.payment_service.dto.request.CreatePaymentRequest;
import com.pranav.payment_service.dto.request.ValidateCredentialRequest;
import com.pranav.payment_service.dto.response.CreatePaymentResponse;
import com.pranav.payment_service.dto.response.MerchantValidationResponse;
import com.pranav.payment_service.dto.response.PaymentResponse;
import com.pranav.payment_service.entity.Payment;
import com.pranav.payment_service.entity.PaymentEvent;
import com.pranav.payment_service.enums.PaymentStatus;
import com.pranav.payment_service.exception.*;
import com.pranav.payment_service.rabbitmq.event.*;
import com.pranav.payment_service.rabbitmq.producer.PaymentEventProducer;
import com.pranav.payment_service.repository.PaymentEventRepository;
import com.pranav.payment_service.repository.PaymentRepository;
import com.pranav.payment_service.util.ReferenceGenerator;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pranav.payment_service.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository      paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final MerchantServiceClient  merchantServiceClient;
    private final PaymentEventProducer   eventProducer;
    private final ReferenceGenerator     referenceGenerator;
    private final ObjectMapper           objectMapper;
    private final RazorpayService        razorpayService;
    private final CircuitBreakerService circuitBreakerService;
//    private final PaymentIndexService paymentIndexService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    // ─── Create Payment ───────────────────────────────────────────────────────

    @Transactional
    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request) {

        // 1. Validate API credentials via merchant-service
        MerchantValidationResponse validation = circuitBreakerService
                .validateMerchantCredentials(
                        new ValidateCredentialRequest() {{
                            setPublicKey(request.getPublicKey());
                            setSecretKey(request.getSecretKey());
                        }});

        if (!Boolean.TRUE.equals(validation.getValid())) {
            throw new InvalidCredentialException("Invalid API credentials.");
        }

        if (!"ACTIVE".equals(validation.getMerchantStatus())) {
            throw new MerchantValidationException(
                    "Merchant account is not active: "
                            + validation.getMerchantStatus());
        }

        if (!Boolean.TRUE.equals(validation.getKycVerified())) {
            throw new MerchantValidationException(
                    "Merchant KYC is not verified.");
        }

        Long merchantId = validation.getMerchantId();

        // 2. Prevent duplicate orders
        if (paymentRepository.existsByMerchantOrderIdAndMerchantId(
                request.getMerchantOrderId(), merchantId)) {
            throw new InvalidPaymentStateException(
                    "Payment already exists for order: "
                            + request.getMerchantOrderId());
        }

        // 3. Create Razorpay order first
        String paymentReference = referenceGenerator
                .generatePaymentReference();
        String razorpayOrderId;

        try {
            razorpayOrderId = circuitBreakerService.createRazorpayOrderWithBreaker(
                    request.getAmount(),
                    request.getCurrency(),
                    paymentReference
            );
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException(
                    "Payment gateway error. Please try again.");
        }

        // 4. Save payment with razorpayOrderId
        Payment payment = Payment.builder()
                .merchantId(merchantId)
                .paymentReference(paymentReference)
                .merchantOrderId(request.getMerchantOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .description(request.getDescription())
                .publicKey(request.getPublicKey())
                .razorpayOrderId(razorpayOrderId)
                .build();

        paymentRepository.save(payment);
//        paymentIndexService.indexPayment(payment);
        savePaymentEvent(payment.getId(), "PAYMENT_CREATED", payment);

        // 5. Publish Kafka event
        eventProducer.publishPaymentCreated(
                PaymentCreatedEvent.builder()
                        .paymentId(payment.getId())
                        .merchantId(merchantId)
                        .paymentReference(paymentReference)
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .customerEmail(payment.getCustomerEmail())
                        .customerName(payment.getCustomerName())
                        .build()
        );

        log.info("Payment created: {} razorpayOrderId: {}",
                paymentReference, razorpayOrderId);

        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentReference(paymentReference)
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayKeyId)
                .amountInPaise(payment.getAmount()
                        .multiply(BigDecimal.valueOf(100)))
                .build();
    }

    // ─── Handle Webhook from Razorpay ─────────────────────────────────────────

    @Transactional
    public void handleWebhook(String payload, String signature) {

        // 1. Verify webhook is genuinely from Razorpay
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            throw new InvalidCredentialException(
                    "Invalid webhook signature");
        }

        try {
            // 2. Parse webhook payload
            com.fasterxml.jackson.databind.JsonNode root =
                    objectMapper.readTree(payload);

            String event = root.path("event").asText();
            log.info("Razorpay webhook received: {}", event);

            switch (event) {
                case "payment.captured" -> handlePaymentCaptured(root);
                case "payment.failed"   -> handlePaymentFailed(root);
                case "refund.created"   -> handleRefundWebhook(root);
                default -> log.info(
                        "Unhandled webhook event: {}", event);
            }

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed");
        }
    }

    // ─── payment.captured ─────────────────────────────────────────────────────

    private void handlePaymentCaptured(
            com.fasterxml.jackson.databind.JsonNode root) {

        String razorpayOrderId  = root
                .path("payload").path("payment")
                .path("entity").path("order_id").asText();
        String razorpayPaymentId = root
                .path("payload").path("payment")
                .path("entity").path("id").asText();

        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + razorpayOrderId));

        // Idempotency — skip if already SUCCESS
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already captured, skipping: {}",
                    razorpayOrderId);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);
//        paymentIndexService.indexPayment(payment);

        savePaymentEvent(payment.getId(), "PAYMENT_CAPTURED", payment);

        // Publish success event → wallet credited + email sent
        eventProducer.publishPaymentSuccess(
                PaymentSuccessEvent.builder()
                        .paymentId(payment.getId())
                        .merchantId(payment.getMerchantId())
                        .paymentReference(payment.getPaymentReference())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .customerEmail(payment.getCustomerEmail())
                        .build()
        );

        log.info("Payment captured: {} razorpayPaymentId: {}",
                payment.getPaymentReference(), razorpayPaymentId);
    }

    // ─── payment.failed ───────────────────────────────────────────────────────

    private void handlePaymentFailed(
            com.fasterxml.jackson.databind.JsonNode root) {

        String razorpayOrderId = root
                .path("payload").path("payment")
                .path("entity").path("order_id").asText();
        String errorDesc = root
                .path("payload").path("payment")
                .path("entity").path("error_description").asText();

        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + razorpayOrderId));

        if (payment.getStatus() == PaymentStatus.FAILED) return;

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
//        paymentIndexService.indexPayment(payment);
        savePaymentEvent(payment.getId(), "PAYMENT_FAILED", payment);

        eventProducer.publishPaymentFailed(
                PaymentFailedEvent.builder()
                        .paymentId(payment.getId())
                        .merchantId(payment.getMerchantId())
                        .paymentReference(payment.getPaymentReference())
                        .amount(payment.getAmount())
                        .reason(errorDesc)
                        .customerEmail(payment.getCustomerEmail())
                        .build()
        );

        log.info("Payment failed for order: {} reason: {}",
                razorpayOrderId, errorDesc);
    }

    // ─── refund.created (from Razorpay) ──────────────────────────────────────

    private void handleRefundWebhook(
            com.fasterxml.jackson.databind.JsonNode root) {
        // Razorpay confirms refund was processed on their side
        // Our refund record was already created when merchant called
        // POST /api/payments/{id}/refund
        String razorpayRefundId = root
                .path("payload").path("refund")
                .path("entity").path("id").asText();
        log.info("Razorpay refund confirmed: {}", razorpayRefundId);
    }

    @Transactional
    public CreatePaymentResponse initiateExistingPayment(UUID paymentId,
                                                         String customerEmail) {
        Payment payment = findById(paymentId);

        // Security: only the customer who owns this payment can initiate
        if (!customerEmail.equals(payment.getCustomerEmail())) {
            throw new InvalidPaymentStateException(
                    "You are not authorized to pay this payment.");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Only PENDING payments can be initiated. Current: "
                            + payment.getStatus());
        }

        // If Razorpay order already created, reuse it
        // If not, create a new one
        String razorpayOrderId = payment.getRazorpayOrderId();

        if (razorpayOrderId == null || razorpayOrderId.isEmpty()) {
            try {
                razorpayOrderId = circuitBreakerService
                        .createRazorpayOrderWithBreaker(
                                payment.getAmount(),
                                payment.getCurrency(),
                                payment.getPaymentReference()
                        );
                payment.setRazorpayOrderId(razorpayOrderId);
                paymentRepository.save(payment);
            } catch (Exception e) {
                log.error("Razorpay order creation failed: {}", e.getMessage());
                throw new RuntimeException(
                        "Payment gateway error. Please try again.");
            }
        }

        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayKeyId)
                .amountInPaise(payment.getAmount()
                        .multiply(BigDecimal.valueOf(100)))
                .build();
    }

    // All payments — admin only, with pagination
    public Page<PaymentResponse> getAllPayments(Pageable pageable,
                                                String status,
                                                String search) {
        Page<Payment> payments;

        if (status != null && search != null) {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
            payments = paymentRepository
                    .findByStatusAndCustomerEmailContainingIgnoreCase(
                            paymentStatus, search, pageable);

        } else if (status != null) {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
            payments = paymentRepository
                    .findByStatus(paymentStatus, pageable);

        } else if (search != null) {
            payments = paymentRepository
                    .findByCustomerEmailContainingIgnoreCase(search, pageable);

        } else {
            payments = paymentRepository.findAll(pageable);
        }

        return payments.map(this::toResponse);
    }

    // ─── Get Payment ──────────────────────────────────────────────────────────
    public List<PaymentResponse> getPaymentsByCustomerEmail(String email) {
        return paymentRepository.findByCustomerEmail(email)
                .stream().map(this::toResponse).toList();
    }

    public PaymentResponse getPayment(UUID paymentId) {
        return toResponse(findById(paymentId));
    }

    public List<PaymentResponse> getMerchantPayments(Long merchantId) {
        return paymentRepository.findByMerchantId(merchantId)
                .stream().map(this::toResponse).toList();
    }

    // ─── Cancel Payment ───────────────────────────────────────────────────────

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Only PENDING payments can be cancelled. Current: "
                            + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
//        paymentIndexService.indexPayment(payment);
        savePaymentEvent(paymentId, "PAYMENT_CANCELLED", payment);

        eventProducer.publishPaymentFailed(
                PaymentFailedEvent.builder()
                        .paymentId(payment.getId())
                        .merchantId(payment.getMerchantId())
                        .paymentReference(payment.getPaymentReference())
                        .amount(payment.getAmount())
                        .reason("Cancelled by merchant")
                        .customerEmail(payment.getCustomerEmail())
                        .build()
        );

        return toResponse(payment);
    }

    public boolean validatePayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .orElse(false);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public Payment findById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));
    }

    private void savePaymentEvent(UUID paymentId,
                                  String eventType,
                                  Object payload) {
        try {
            paymentEventRepository.save(
                    PaymentEvent.builder()
                            .paymentId(paymentId)
                            .eventType(eventType)
                            .payload(objectMapper.writeValueAsString(payload))
                            .build()
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event payload: {}",
                    e.getMessage());
        }
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getId())
                .merchantId(p.getMerchantId())
                .paymentReference(p.getPaymentReference())
                .merchantOrderId(p.getMerchantOrderId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .paymentMethod(p.getPaymentMethod() != null
                        ? p.getPaymentMethod().name() : null)
                .customerName(p.getCustomerName())
                .customerEmail(p.getCustomerEmail())
                .customerPhone(p.getCustomerPhone())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}