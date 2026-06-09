package com.pranav.wallet_service.service;

import com.pranav.wallet_service.dto.request.SettlementRequest;
import com.pranav.wallet_service.dto.response.SettlementResponse;
import com.pranav.wallet_service.entity.Settlement;
import com.pranav.wallet_service.entity.Wallet;
import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.enums.SettlementStatus;
import com.pranav.wallet_service.enums.TransactionType;
import com.pranav.wallet_service.exception.InsufficientBalanceException;
import com.pranav.wallet_service.exception.SettlementNotFoundException;
import com.pranav.wallet_service.repository.SettlementRepository;
import com.pranav.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository        settlementRepository;
    private final WalletService               walletService;
    private final WalletTransactionRepository transactionRepository;

    // ─── Request Settlement ───────────────────────────────────────────────────

    @Transactional
    public SettlementResponse requestSettlement(Long merchantId,
                                                SettlementRequest request) {
        Wallet wallet = walletService.findByMerchantId(merchantId);

        // Check sufficient balance
        if (wallet.getAvailableBalance()
                .compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for settlement. Available: "
                            + wallet.getAvailableBalance()
                            + ", Requested: " + request.getAmount());
        }

        // Create settlement record
        Settlement settlement = settlementRepository.save(
                Settlement.builder()
                        .merchantId(merchantId)
                        .walletId(wallet.getId())
                        .amount(request.getAmount())
                        .status(SettlementStatus.PENDING)
                        .build()
        );

        // Debit wallet immediately — money reserved for settlement
        walletService.debit(
                merchantId,
                request.getAmount(),
                settlement.getId().toString(),
                ReferenceType.SETTLEMENT,
                "Settlement request #" + settlement.getId()
        );

        log.info("Settlement #{} created for merchant {} amount {}",
                settlement.getId(), merchantId, request.getAmount());

        return toResponse(settlement);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public SettlementResponse getSettlement(Long settlementId) {
        return toResponse(findById(settlementId));
    }

    public List<SettlementResponse> getMerchantSettlements(Long merchantId) {
        return settlementRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Settlement findById(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new SettlementNotFoundException(
                        "Settlement not found: " + id));
    }

    private SettlementResponse toResponse(Settlement s) {
        return SettlementResponse.builder()
                .settlementId(s.getId())
                .merchantId(s.getMerchantId())
                .walletId(s.getWalletId())
                .amount(s.getAmount())
                .status(s.getStatus().name())
                .bankReference(s.getBankReference())
                .createdAt(s.getCreatedAt())
                .build();
    }
}