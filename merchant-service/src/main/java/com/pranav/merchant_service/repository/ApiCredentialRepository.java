package com.pranav.merchant_service.repository;

import com.pranav.merchant_service.entity.ApiCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiCredentialRepository
        extends JpaRepository<ApiCredential, Long> {

    Optional<ApiCredential> findByPublicKey(String publicKey);

    List<ApiCredential> findByMerchantId(Long merchantId);
}