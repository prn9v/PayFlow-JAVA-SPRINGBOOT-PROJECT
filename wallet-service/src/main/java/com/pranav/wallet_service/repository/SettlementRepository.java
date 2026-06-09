// SettlementRepository.java
package com.pranav.wallet_service.repository;

import com.pranav.wallet_service.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository
        extends JpaRepository<Settlement, Long> {

    List<Settlement> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
}