package com.pranav.merchant_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "bank_accounts",
        indexes = {
                @Index(name = "idx_bank_merchant_id", columnList = "merchant_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @NotBlank
    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Pattern(
            regexp = "^[0-9]{9,18}$",
            message = "Invalid account number"
    )
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Pattern(
            regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC code"
    )
    @Column(name = "ifsc_code", nullable = false)
    private String ifscCode;

    @NotBlank
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "primary_account", nullable = false)
    private Boolean primaryAccount = false;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (primaryAccount == null) {
            primaryAccount = false;
        }

        if (verified == null) {
            verified = false;
        }
    }
}