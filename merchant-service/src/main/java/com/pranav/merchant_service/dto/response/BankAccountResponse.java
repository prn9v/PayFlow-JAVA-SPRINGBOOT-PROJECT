package com.pranav.merchant_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {
    private Long id;
    private Long merchantId;
    private String accountHolderName;
    private String accountNumber;   // masked in service layer
    private String ifscCode;
    private String bankName;
    private Boolean primaryAccount;
    private Boolean verified;
    private LocalDateTime createdAt;
}