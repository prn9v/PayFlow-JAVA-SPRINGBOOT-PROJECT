// WalletTransactionRepository.java
package com.pranav.wallet_service.repository;

import com.pranav.wallet_service.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    List<WalletTransaction> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    // Prevent duplicate processing of same Kafka event
    boolean existsByReferenceId(String referenceId);
}