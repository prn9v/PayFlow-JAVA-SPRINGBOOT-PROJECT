package com.pranav.merchant_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    private Long merchantId;
    private Long userId;
    private String businessName;
    private String businessEmail;
    private String businessPhone;
    private String website;
    private String businessAddress;
    private String panNumber;
    private String status;
    private Boolean kycVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}