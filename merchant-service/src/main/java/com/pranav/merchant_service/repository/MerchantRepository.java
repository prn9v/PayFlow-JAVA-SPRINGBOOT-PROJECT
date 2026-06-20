package com.pranav.merchant_service.repository;

import com.pranav.merchant_service.entity.Merchant;
import com.pranav.merchant_service.enums.MerchantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository
        extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByUserId(Long userId);

    Optional<Merchant> findByBusinessEmail(String businessEmail);

    boolean existsByPanNumber(String panNumber);

    boolean existsByBusinessEmail(String businessEmail);

    // ── Paginated queries for admin ───────────────────────────────────────────
    Page<Merchant> findAll(Pageable pageable);

    Page<Merchant> findByStatus(MerchantStatus status, Pageable pageable);

    Page<Merchant> findByBusinessNameContainingIgnoreCase(
            String search, Pageable pageable);

    Page<Merchant> findByStatusAndBusinessNameContainingIgnoreCase(
            MerchantStatus status, String search, Pageable pageable);
}