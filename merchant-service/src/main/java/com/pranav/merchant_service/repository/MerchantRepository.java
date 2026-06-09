package com.pranav.merchant_service.repository;

import com.pranav.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository
        extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByUserId(Long userId);

    Optional<Merchant> findByBusinessEmail(String businessEmail);

    boolean existsByPanNumber(String panNumber);

    boolean existsByBusinessEmail(String businessEmail);
}