package com.pranav.notification_service.rabbitmq.consumer;

import com.pranav.notification_service.config.RabbitMQConfig;
import com.pranav.notification_service.rabbitmq.event.*;
import com.pranav.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_CREATED)
    public void onPaymentCreated(PaymentCreatedEvent event) {
        log.info("Received payment.created for ref={}",
                event.getPaymentReference());
        try {
            notificationService.sendPaymentCreatedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment.created: {}",
                    e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_SUCCESS)
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment.success for ref={}",
                event.getPaymentReference());
        try {
            notificationService.sendPaymentSuccessEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment.success: {}",
                    e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_FAILED)
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment.failed for ref={}",
                event.getPaymentReference());
        try {
            notificationService.sendPaymentFailedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment.failed: {}",
                    e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REFUND_CREATED)
    public void onRefundCreated(RefundCreatedEvent event) {
        log.info("Received refund.created for paymentId={}",
                event.getPaymentId());
        try {
            notificationService.sendRefundCreatedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process refund.created: {}",
                    e.getMessage());
        }
    }
}