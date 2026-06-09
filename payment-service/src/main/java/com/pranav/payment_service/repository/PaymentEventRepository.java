// PaymentEventRepository.java
package com.pranav.payment_service.repository;

import com.pranav.payment_service.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);
}