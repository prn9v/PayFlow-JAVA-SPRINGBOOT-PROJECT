package com.pranav.merchant_service.service;

import com.pranav.merchant_service.dto.request.AddBankAccountRequest;
import com.pranav.merchant_service.dto.request.UpdateBankAccountRequest;
import com.pranav.merchant_service.dto.response.BankAccountResponse;
import com.pranav.merchant_service.entity.BankAccount;
import com.pranav.merchant_service.entity.Merchant;
import com.pranav.merchant_service.exception.BankAccountNotFoundException;
import com.pranav.merchant_service.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final MerchantService merchantService;

    // ─── Add ──────────────────────────────────────────────────────────────────

    @Transactional
    public BankAccountResponse addBankAccount(Long merchantId,
                                              AddBankAccountRequest request) {
        Merchant merchant = merchantService.findById(merchantId);

        // If first account or explicitly set as primary — unset existing primary
        if (Boolean.TRUE.equals(request.getPrimaryAccount())) {
            unsetCurrentPrimary(merchantId);
        }

        BankAccount account = BankAccount.builder()
                .merchant(merchant)
                .accountHolderName(request.getAccountHolderName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .primaryAccount(Boolean.TRUE.equals(request.getPrimaryAccount()))
                .verified(false)
                .build();

        return toResponse(bankAccountRepository.save(account));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<BankAccountResponse> getBankAccounts(Long merchantId) {
        merchantService.findById(merchantId); // Validate merchant exists
        return bankAccountRepository.findByMerchant_Id(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BankAccountResponse getBankAccount(Long merchantId, Long bankAccountId) {
        return toResponse(findByIdAndMerchant(merchantId, bankAccountId));
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public BankAccountResponse updateBankAccount(Long merchantId,
                                                 Long bankAccountId,
                                                 UpdateBankAccountRequest request) {
        BankAccount account = findByIdAndMerchant(merchantId, bankAccountId);

        if (request.getAccountHolderName() != null)
            account.setAccountHolderName(request.getAccountHolderName());
        if (request.getIfscCode() != null)
            account.setIfscCode(request.getIfscCode());
        if (request.getBankName() != null)
            account.setBankName(request.getBankName());

        return toResponse(bankAccountRepository.save(account));
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void deleteBankAccount(Long merchantId, Long bankAccountId) {
        BankAccount account = findByIdAndMerchant(merchantId, bankAccountId);
        bankAccountRepository.delete(account);
    }

    // ─── Set Primary ──────────────────────────────────────────────────────────

    @Transactional
    public BankAccountResponse setPrimary(Long merchantId, Long bankAccountId) {
        unsetCurrentPrimary(merchantId);
        BankAccount account = findByIdAndMerchant(merchantId, bankAccountId);
        account.setPrimaryAccount(true);
        return toResponse(bankAccountRepository.save(account));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void unsetCurrentPrimary(Long merchantId) {
        bankAccountRepository
                .findByMerchant_IdAndPrimaryAccountTrue(merchantId)
                .ifPresent(existing -> {
                    existing.setPrimaryAccount(false);
                    bankAccountRepository.save(existing);
                });
    }

    private BankAccount findByIdAndMerchant(Long merchantId, Long bankAccountId) {
        return bankAccountRepository
                .findByIdAndMerchant_Id(bankAccountId, merchantId)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Bank account not found with id: " + bankAccountId));
    }

    // Mask account number: show only last 4 digits
    private BankAccountResponse toResponse(BankAccount a) {
        String masked = "****" + a.getAccountNumber()
                .substring(Math.max(0, a.getAccountNumber().length() - 4));
        return BankAccountResponse.builder()
                .id(a.getId())
                .merchantId(a.getMerchant().getId())
                .accountHolderName(a.getAccountHolderName())
                .accountNumber(masked)
                .ifscCode(a.getIfscCode())
                .bankName(a.getBankName())
                .primaryAccount(a.getPrimaryAccount())
                .verified(a.getVerified())
                .createdAt(a.getCreatedAt())
                .build();
    }
}