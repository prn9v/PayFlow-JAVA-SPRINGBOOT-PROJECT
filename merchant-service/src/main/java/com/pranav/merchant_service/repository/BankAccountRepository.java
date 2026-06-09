package com.pranav.merchant_service.repository;

import com.pranav.merchant_service.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByMerchant_Id(Long merchantId);

    Optional<BankAccount> findByIdAndMerchant_Id(Long id, Long merchantId);

    // For enforcing single primary account per merchant
    Optional<BankAccount> findByMerchant_IdAndPrimaryAccountTrue(Long merchantId);
}