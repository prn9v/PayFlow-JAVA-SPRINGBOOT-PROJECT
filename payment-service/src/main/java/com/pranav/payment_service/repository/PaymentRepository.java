package com.pranav.payment_service.repository;

import com.pranav.payment_service.entity.Payment;
import com.pranav.payment_service.enums.PaymentStatus;
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
}