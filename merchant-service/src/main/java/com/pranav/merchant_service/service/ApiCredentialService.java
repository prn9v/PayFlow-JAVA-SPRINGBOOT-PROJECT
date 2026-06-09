package com.pranav.merchant_service.service;

import com.pranav.merchant_service.dto.cache.CredentialCacheDto;
import com.pranav.merchant_service.dto.response.ApiCredentialResponse;
import com.pranav.merchant_service.entity.ApiCredential;
import com.pranav.merchant_service.entity.Merchant;
import com.pranav.merchant_service.exception.ApiCredentialNotFoundException;
import com.pranav.merchant_service.exception.InvalidCredentialException;
import com.pranav.merchant_service.repository.ApiCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCredentialService {

    private final ApiCredentialRepository apiCredentialRepository;
    private final MerchantService         merchantService;
    private final BCryptPasswordEncoder   encoder = new BCryptPasswordEncoder();
    private final SecureRandom            secureRandom = new SecureRandom();

    // ─── Cached credential lookup ─────────────────────────────────────────────

    @Cacheable(value = "merchantCredentials", key = "#publicKey")
    public CredentialCacheDto getCredentialForValidation(String publicKey) {
        log.info("Cache MISS — loading credentials from DB for key: {}",
                publicKey);

        ApiCredential credential = apiCredentialRepository
                .findByPublicKey(publicKey)
                .orElseThrow(() -> new InvalidCredentialException(
                        "Invalid credentials"));

        Merchant merchant = merchantService.findById(
                credential.getMerchantId());

        return CredentialCacheDto.builder()
                .merchantId(merchant.getId())
                .merchantStatus(merchant.getStatus().name())
                .kycVerified(merchant.getKycVerified())
                .secretKeyHash(credential.getSecretKeyHash())
                .active(credential.getActive())
                .build();
    }

    // ─── Validate credentials (called by internal endpoint) ───────────────────

    public Map<String, Object> validateCredentials(String publicKey,
                                                   String secretKey) {
        // Fetch from cache (or DB on miss)
        CredentialCacheDto cached = getCredentialForValidation(publicKey);

        if (!Boolean.TRUE.equals(cached.getActive())) {
            throw new InvalidCredentialException(
                    "API credentials are disabled");
        }

        if (!encoder.matches(secretKey, cached.getSecretKeyHash())) {
            throw new InvalidCredentialException("Invalid credentials");
        }

        return Map.of(
                "merchantId",     cached.getMerchantId(),
                "merchantStatus", cached.getMerchantStatus(),
                "kycVerified",    cached.getKycVerified(),
                "valid",          true
        );
    }

    // ─── Generate — evicts old cache entry ────────────────────────────────────

    @Transactional
    public ApiCredentialResponse generateCredentials(Long merchantId) {
        merchantService.findById(merchantId);

        // Evict old credentials from cache before generating new ones
        List<ApiCredential> existing = apiCredentialRepository
                .findByMerchantId(merchantId);
        existing.forEach(c -> {
            evictCredentialCache(c.getPublicKey());
            c.setActive(false);
            apiCredentialRepository.save(c);
        });

        String publicKey  = "pk_" + UUID.randomUUID()
                .toString().replace("-", "");
        String secretKey  = "sk_" + generateSecureToken();
        String secretHash = encoder.encode(secretKey);

        ApiCredential credential = ApiCredential.builder()
                .merchantId(merchantId)
                .publicKey(publicKey)
                .secretKeyHash(secretHash)
                .active(true)
                .build();

        apiCredentialRepository.save(credential);
        return toResponse(credential, secretKey);
    }

    // ─── Rotate — evict and regenerate ───────────────────────────────────────

    @Transactional
    public ApiCredentialResponse rotateKeys(Long merchantId) {
        ApiCredential existing = getActiveCredential(merchantId);

        // Evict old cache entry
        evictCredentialCache(existing.getPublicKey());

        String newSecret     = "sk_" + generateSecureToken();
        String newSecretHash = encoder.encode(newSecret);

        existing.setSecretKeyHash(newSecretHash);
        existing.setLastRotatedAt(LocalDateTime.now());
        apiCredentialRepository.save(existing);

        return toResponse(existing, newSecret);
    }

    // ─── Disable — evict cache ────────────────────────────────────────────────

    @Transactional
    public ApiCredentialResponse disableCredentials(Long merchantId) {
        ApiCredential credential = getActiveCredential(merchantId);

        // Evict from cache immediately
        evictCredentialCache(credential.getPublicKey());

        credential.setActive(false);
        return toResponse(apiCredentialRepository.save(credential), null);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<ApiCredentialResponse> getCredentials(Long merchantId) {
        merchantService.findById(merchantId);
        return apiCredentialRepository.findByMerchantId(merchantId)
                .stream()
                .map(c -> toResponse(c, null))
                .toList();
    }

    // ─── Cache eviction helper ────────────────────────────────────────────────

    @CacheEvict(value = "merchantCredentials", key = "#publicKey")
    public void evictCredentialCache(String publicKey) {
        log.info("Cache EVICT — merchantCredentials for key: {}", publicKey);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private ApiCredential getActiveCredential(Long merchantId) {
        return apiCredentialRepository.findByMerchantId(merchantId)
                .stream()
                .filter(ApiCredential::getActive)
                .findFirst()
                .orElseThrow(() -> new ApiCredentialNotFoundException(
                        "No active API credentials found for merchant: "
                                + merchantId));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApiCredentialResponse toResponse(ApiCredential c,
                                             String rawSecret) {
        return ApiCredentialResponse.builder()
                .id(c.getId())
                .merchantId(c.getMerchantId())
                .publicKey(c.getPublicKey())
                .secretKey(rawSecret)
                .active(c.getActive())
                .lastRotatedAt(c.getLastRotatedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}