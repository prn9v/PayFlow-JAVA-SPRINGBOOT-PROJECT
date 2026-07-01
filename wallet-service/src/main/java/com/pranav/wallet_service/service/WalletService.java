package com.pranav.wallet_service.service;

import com.pranav.wallet_service.dto.response.WalletResponse;
import com.pranav.wallet_service.dto.response.WalletTransactionResponse;
import com.pranav.wallet_service.entity.Wallet;
import com.pranav.wallet_service.entity.WalletTransaction;
import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.enums.TransactionType;
import com.pranav.wallet_service.exception.InsufficientBalanceException;
import com.pranav.wallet_service.exception.WalletNotFoundException;
import com.pranav.wallet_service.repository.WalletRepository;
import com.pranav.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository            walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Value("${wallet.default-currency}")
    private String defaultCurrency;

    // ─── Create wallet for new merchant ──────────────────────────────────────

    @Transactional
    public void createWallet(Long merchantId) {
        if (walletRepository.existsByMerchantId(merchantId)) {
            log.warn("Wallet already exists for merchant: {}", merchantId);
            return;
        }

        Wallet wallet = Wallet.builder()
                .merchantId(merchantId)
                .availableBalance(BigDecimal.ZERO)
                .pendingBalance(BigDecimal.ZERO)
                .currency(defaultCurrency)
                .active(true)
                .build();

        walletRepository.save(wallet);
        log.info("Wallet created for merchant: {}", merchantId);
    }

    // ─── Credit ───────────────────────────────────────────────────────────────

    @Transactional
    public void credit(Long merchantId,
                       BigDecimal amount,
                       String referenceId,
                       ReferenceType referenceType,
                       String description) {

        // Idempotency check — never credit same reference twice
        if (transactionRepository.existsByReferenceId(referenceId)) {
            log.warn("Duplicate credit ignored for referenceId: {}", referenceId);
            return;
        }

        Wallet wallet;
        try {
            wallet = findByMerchantId(merchantId);
        } catch (WalletNotFoundException e) {
            log.info("Wallet not found for merchant: {}. Creating one automatically.", merchantId);
            createWallet(merchantId);
            wallet = findByMerchantId(merchantId);
        }

        BigDecimal balanceBefore = wallet.getAvailableBalance();
        BigDecimal balanceAfter  = balanceBefore.add(amount);

        wallet.setAvailableBalance(balanceAfter);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, balanceBefore, balanceAfter,
                TransactionType.CREDIT, referenceId, referenceType, description);

        log.info("Credited {} to merchant {} wallet. New balance: {}",
                amount, merchantId, balanceAfter);
    }

    // ─── Debit ────────────────────────────────────────────────────────────────

    @Transactional
    public void debit(Long merchantId,
                      BigDecimal amount,
                      String referenceId,
                      ReferenceType referenceType,
                      String description) {

        // Idempotency check — never debit same reference twice
        if (transactionRepository.existsByReferenceId(referenceId)) {
            log.warn("Duplicate debit ignored for referenceId: {}", referenceId);
            return;
        }

        Wallet wallet = findByMerchantId(merchantId);

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: "
                            + wallet.getAvailableBalance()
                            + ", Requested: " + amount);
        }

        BigDecimal balanceBefore = wallet.getAvailableBalance();
        BigDecimal balanceAfter  = balanceBefore.subtract(amount);

        wallet.setAvailableBalance(balanceAfter);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, balanceBefore, balanceAfter,
                TransactionType.DEBIT, referenceId, referenceType, description);

        log.info("Debited {} from merchant {} wallet. New balance: {}",
                amount, merchantId, balanceAfter);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public WalletResponse getWalletByMerchantId(Long merchantId) {
        return toResponse(findByMerchantId(merchantId));
    }

    public List<WalletTransactionResponse> getTransactions(Long merchantId) {
        Wallet wallet = findByMerchantId(merchantId);
        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public WalletTransactionResponse getTransaction(Long transactionId) {
        return toTransactionResponse(
                transactionRepository.findById(transactionId)
                        .orElseThrow(() -> new WalletNotFoundException(
                                "Transaction not found: " + transactionId))
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public Wallet findByMerchantId(Long merchantId) {
        return walletRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found for merchant: " + merchantId));
    }

    private void saveTransaction(Wallet wallet,
                                 BigDecimal amount,
                                 BigDecimal balanceBefore,
                                 BigDecimal balanceAfter,
                                 TransactionType type,
                                 String referenceId,
                                 ReferenceType referenceType,
                                 String description) {
        transactionRepository.save(
                WalletTransaction.builder()
                        .walletId(wallet.getId())
                        .merchantId(wallet.getMerchantId())
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .transactionType(type)
                        .amount(amount)
                        .balanceBefore(balanceBefore)
                        .balanceAfter(balanceAfter)
                        .description(description)
                        .build()
        );
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .walletId(w.getId())
                .merchantId(w.getMerchantId())
                .availableBalance(w.getAvailableBalance())
                .pendingBalance(w.getPendingBalance())
                .currency(w.getCurrency())
                .active(w.getActive())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private WalletTransactionResponse toTransactionResponse(
            WalletTransaction t) {
        return WalletTransactionResponse.builder()
                .id(t.getId())
                .walletId(t.getWalletId())
                .merchantId(t.getMerchantId())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType().name())
                .transactionType(t.getTransactionType().name())
                .amount(t.getAmount())
                .balanceBefore(t.getBalanceBefore())
                .balanceAfter(t.getBalanceAfter())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}