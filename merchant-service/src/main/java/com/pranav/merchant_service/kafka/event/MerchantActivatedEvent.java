package com.pranav.merchant_service.kafka.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantActivatedEvent {
    private Long   merchantId;
    private String businessName;
    private String businessEmail;
}