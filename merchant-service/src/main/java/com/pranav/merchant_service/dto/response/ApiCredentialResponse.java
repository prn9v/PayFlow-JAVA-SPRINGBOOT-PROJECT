package com.pranav.merchant_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentialResponse {
    private Long id;
    private Long merchantId;
    private String publicKey;
    private String secretKey;   // Only returned ONCE on generation; null otherwise
    private Boolean active;
    private LocalDateTime lastRotatedAt;
    private LocalDateTime createdAt;
}