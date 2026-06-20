package com.pranav.payment_service.repository;

import com.pranav.payment_service.entity.Payment;
import com.pranav.payment_service.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByMerchantId(Long merchantId);

    List<Payment> findByMerchantIdAndStatus(Long merchantId,
                                            PaymentStatus status);

    boolean existsByMerchantOrderIdAndMerchantId(String merchantOrderId,
                                                 Long merchantId);

    // ── Customer payments ──────────────────────────────────────────────────────
    List<Payment> findByCustomerEmail(String customerEmail);

    List<Payment> findByCustomerEmailAndStatus(String customerEmail,
                                               PaymentStatus status);

    // ── Admin paginated queries ────────────────────────────────────────────────
    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByCustomerEmailContainingIgnoreCase(
            String search, Pageable pageable);

    Page<Payment> findByStatusAndCustomerEmailContainingIgnoreCase(
            PaymentStatus status, String search, Pageable pageable);
}