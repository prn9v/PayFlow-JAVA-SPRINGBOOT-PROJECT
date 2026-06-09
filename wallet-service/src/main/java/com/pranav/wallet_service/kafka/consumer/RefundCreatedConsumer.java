package com.pranav.wallet_service.kafka.consumer;

import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.kafka.event.RefundCreatedEvent;
import com.pranav.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundCreatedConsumer {

    private final WalletService walletService;

    @KafkaListener(
            topics           = "refund-created",
            groupId          = "wallet-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(RefundCreatedEvent event) {  // ← typed directly
        try {
            log.info("Consumed refund-created event: {}",
                    event.getRefundReference());
            walletService.debit(
                    event.getMerchantId(),
                    event.getAmount(),
                    event.getRefundId().toString(),
                    ReferenceType.REFUND,
                    "Refund processed: " + event.getRefundReference()
            );
        } catch (Exception e) {
            log.error("Error processing refund-created event: {}",
                    e.getMessage(), e);
        }
    }
}