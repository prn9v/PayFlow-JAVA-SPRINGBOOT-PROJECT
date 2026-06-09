package com.pranav.wallet_service.kafka.consumer;

import com.pranav.wallet_service.kafka.event.MerchantActivatedEvent;
import com.pranav.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantActivatedConsumer {

    private final WalletService walletService;

    @KafkaListener(
            topics           = "merchant-activated",
            groupId          = "wallet-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(MerchantActivatedEvent event) {  // ← typed directly
        try {
            log.info("Consumed merchant-activated event for merchantId: {}",
                    event.getMerchantId());
            walletService.createWallet(event.getMerchantId());
        } catch (Exception e) {
            log.error("Error processing merchant-activated event: {}",
                    e.getMessage(), e);
        }
    }
}