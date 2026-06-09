package com.pranav.merchant_service.dto.cache;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialCacheDto implements Serializable {
    private Long    merchantId;
    private String  merchantStatus;
    private Boolean kycVerified;
    private String  secretKeyHash;   // BCrypt hash — for validation
    private Boolean active;
}