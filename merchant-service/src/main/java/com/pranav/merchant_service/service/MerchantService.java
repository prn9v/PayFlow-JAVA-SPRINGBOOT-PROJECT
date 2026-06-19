package com.pranav.merchant_service.service;

import com.pranav.merchant_service.dto.request.CreateMerchantRequest;
import com.pranav.merchant_service.dto.request.UpdateMerchantRequest;
import com.pranav.merchant_service.dto.response.MerchantResponse;
import com.pranav.merchant_service.entity.Merchant;
import com.pranav.merchant_service.enums.MerchantStatus;
import com.pranav.merchant_service.exception.MerchantAlreadyExistsException;
import com.pranav.merchant_service.exception.MerchantNotFoundException;
import com.pranav.merchant_service.rabbitmq.event.MerchantActivatedEvent;
import com.pranav.merchant_service.rabbitmq.producer.MerchantEventProducer;
import com.pranav.merchant_service.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;


@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantEventProducer merchantEventProducer;

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public MerchantResponse createMerchant(CreateMerchantRequest request,
                                           Long userId) {
        if (merchantRepository.findByUserId(userId).isPresent()) {
            throw new MerchantAlreadyExistsException(
                    "Merchant profile already exists for this user.");
        }
        if (merchantRepository.existsByBusinessEmail(request.getBusinessEmail())) {
            throw new MerchantAlreadyExistsException(
                    "Business email already registered: " + request.getBusinessEmail());
        }
        if (request.getPanNumber() != null &&
                merchantRepository.existsByPanNumber(request.getPanNumber())) {
            throw new MerchantAlreadyExistsException(
                    "PAN number already registered.");
        }

        Merchant merchant = Merchant.builder()
                .userId(userId)
                .businessName(request.getBusinessName())
                .businessEmail(request.getBusinessEmail())
                .businessPhone(request.getBusinessPhone())
                .panNumber(request.getPanNumber())
                .website(request.getWebsite())
                .businessAddress(request.getBusinessAddress())
                .status(MerchantStatus.PENDING)
                .kycVerified(false)
                .build();

        return toResponse(merchantRepository.save(merchant));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────
    @Cacheable(value = "merchants", key = "#merchantId")
    public MerchantResponse getMerchantById(Long merchantId) {
        return toResponse(findById(merchantId));
    }

    public MerchantResponse getMerchantByUserId(Long userId) {
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "No merchant profile found for userId: " + userId));
        return toResponse(merchant);
    }

    // ─── Update ───────────────────────────────────────────────────────────────


    @Transactional
    @CacheEvict(value = "merchants", key = "#merchantId")
    public MerchantResponse updateMerchant(Long merchantId,
                                           UpdateMerchantRequest request) {
        Merchant merchant = findById(merchantId);

        if (request.getBusinessName()    != null)
            merchant.setBusinessName(request.getBusinessName());
        if (request.getBusinessEmail()   != null)
            merchant.setBusinessEmail(request.getBusinessEmail());
        if (request.getBusinessPhone()   != null)
            merchant.setBusinessPhone(request.getBusinessPhone());
        if (request.getWebsite()         != null)
            merchant.setWebsite(request.getWebsite());
        if (request.getBusinessAddress() != null)
            merchant.setBusinessAddress(request.getBusinessAddress());
        if (request.getPanNumber()       != null)
            merchant.setPanNumber(request.getPanNumber());

        return toResponse(merchantRepository.save(merchant));
    }

    // ─── Delete (soft) ────────────────────────────────────────────────────────

    @Transactional
    public void deleteMerchant(Long merchantId) {
        Merchant merchant = findById(merchantId);
        merchant.setStatus(MerchantStatus.SUSPENDED);
        merchantRepository.save(merchant);
    }

    // ─── Status Management ────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "merchants", key = "#merchantId")
    public MerchantResponse activateMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId));
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setKycVerified(true);
        merchantRepository.save(merchant);

        merchantEventProducer.publishMerchantActivated(
                MerchantActivatedEvent.builder()
                        .merchantId(merchant.getId())
                        .businessName(merchant.getBusinessName())
                        .businessEmail(merchant.getBusinessEmail())
                        .build()
        );

        return toResponse(merchant);
    }

    @Transactional
    @CacheEvict(value = "merchants", key = "#merchantId")
    public MerchantResponse suspendMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId));
        merchant.setStatus(MerchantStatus.SUSPENDED);
        return toResponse(merchantRepository.save(merchant));
    }

    @Transactional
    @CacheEvict(value = "merchants", key = "#merchantId")
    public MerchantResponse rejectMerchant(Long merchantId) {
        Merchant merchant = findById(merchantId);
        merchant.setStatus(MerchantStatus.REJECTED);
        return toResponse(merchantRepository.save(merchant));
    }


    // ─── Internal ─────────────────────────────────────────────────────────────

    public boolean validateMerchant(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .map(m -> m.getStatus() == MerchantStatus.ACTIVE)
                .orElse(false);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public Merchant findById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId));
    }

    public MerchantResponse toResponse(Merchant m) {
        return MerchantResponse.builder()
                .merchantId(m.getId())
                .userId(m.getUserId())
                .businessName(m.getBusinessName())
                .businessEmail(m.getBusinessEmail())
                .businessPhone(m.getBusinessPhone())
                .website(m.getWebsite())
                .businessAddress(m.getBusinessAddress())
                .panNumber(m.getPanNumber())
                .status(m.getStatus().name())
                .kycVerified(m.getKycVerified())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}