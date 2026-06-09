// WalletRepository.java
package com.pranav.wallet_service.repository;

import com.pranav.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByMerchantId(Long merchantId);

    boolean existsByMerchantId(Long merchantId);
}