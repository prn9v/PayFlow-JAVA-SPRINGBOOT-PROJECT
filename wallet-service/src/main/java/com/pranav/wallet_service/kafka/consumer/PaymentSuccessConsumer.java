package com.pranav.wallet_service.kafka.consumer;

import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.kafka.event.PaymentSuccessEvent;
import com.pranav.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessConsumer {

    private final WalletService walletService;

    @KafkaListener(
            topics           = "payment-success",
            groupId          = "wallet-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(PaymentSuccessEvent event) {  // ← typed directly
        try {
            log.info("Consumed payment-success event: {}",
                    event.getPaymentReference());
            walletService.credit(
                    event.getMerchantId(),
                    event.getAmount(),
                    event.getPaymentId().toString(),
                    ReferenceType.PAYMENT,
                    "Payment received: " + event.getPaymentReference()
            );
        } catch (Exception e) {
            log.error("Error processing payment-success event: {}",
                    e.getMessage(), e);
        }
    }
}