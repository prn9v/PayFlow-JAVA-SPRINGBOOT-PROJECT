package com.pranav.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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