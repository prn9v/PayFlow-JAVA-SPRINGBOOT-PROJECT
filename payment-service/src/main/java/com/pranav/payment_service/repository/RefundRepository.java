// RefundRepository.java
package com.pranav.payment_service.repository;

import com.pranav.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentId(UUID paymentId);

    Optional<Refund> findByRefundReference(String refundReference);
}