package com.pranav.wallet_service.rabbitmq.consumer;

import com.pranav.wallet_service.config.RabbitMQConfig;
import com.pranav.wallet_service.enums.ReferenceType;
import com.pranav.wallet_service.rabbitmq.event.MerchantActivatedEvent;
import com.pranav.wallet_service.rabbitmq.event.PaymentSuccessEvent;
import com.pranav.wallet_service.rabbitmq.event.RefundCreatedEvent;
import com.pranav.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletConsumer {

    private final WalletService walletService;

    // ─── Merchant Activated → Create Wallet ──────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_MERCHANT_ACTIVATED)
    public void onMerchantActivated(MerchantActivatedEvent event) {
        log.info("Received merchant.activated for merchantId={}",
                event.getMerchantId());
        try {
            walletService.createWallet(event.getMerchantId());
        } catch (Exception e) {
            log.error("Failed to create wallet for merchant {}: {}",
                    event.getMerchantId(), e.getMessage());
        }
    }

    // ─── Payment Success → Credit Wallet ─────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_SUCCESS)
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment.success for paymentId={}",
                event.getPaymentId());
        try {
            walletService.credit(
                    event.getMerchantId(),
                    event.getAmount(),
                    event.getPaymentId().toString(),          // referenceId = paymentId
                    ReferenceType.PAYMENT,         // referenceType
                    "Payment received: "           // description
                            + event.getPaymentReference()
            );
        } catch (Exception e) {
            log.error("Failed to credit wallet for payment {}: {}",
                    event.getPaymentId(), e.getMessage());
        }
    }

    // ─── Refund Created → Debit Wallet ───────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REFUND_CREATED)
    public void onRefundCreated(RefundCreatedEvent event) {
        log.info("Received refund.created for refundId={}",
                event.getRefundId());
        try {
            walletService.debit(
                    event.getMerchantId(),
                    event.getAmount(),
                    event.getRefundId().toString(),           // referenceId = refundId
                    ReferenceType.REFUND,          // referenceType
                    "Refund for payment: "         // description
                            + event.getRefundReference()
            );
        } catch (Exception e) {
            log.error("Failed to debit wallet for refund {}: {}",
                    event.getRefundId(), e.getMessage());
        }
    }
}