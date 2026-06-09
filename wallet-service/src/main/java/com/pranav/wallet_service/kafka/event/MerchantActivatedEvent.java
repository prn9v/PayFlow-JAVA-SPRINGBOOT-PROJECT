// MerchantActivatedEvent.java
package com.pranav.wallet_service.kafka.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantActivatedEvent {
    private Long   merchantId;
    private String businessName;
    private String businessEmail;
}