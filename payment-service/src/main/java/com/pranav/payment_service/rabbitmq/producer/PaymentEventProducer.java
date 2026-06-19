package com.pranav.payment_service.rabbitmq.producer;

import com.pranav.payment_service.config.RabbitMQConfig;
import com.pranav.payment_service.rabbitmq.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        publish(RabbitMQConfig.KEY_PAYMENT_CREATED, event);
    }

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        publish(RabbitMQConfig.KEY_PAYMENT_SUCCESS, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        publish(RabbitMQConfig.KEY_PAYMENT_FAILED, event);
    }

    public void publishRefundCreated(RefundCreatedEvent event) {
        publish(RabbitMQConfig.KEY_REFUND_CREATED, event);
    }

    private void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    routingKey,
                    event
            );
            log.info("Published event to exchange={} routingKey={}",
                    RabbitMQConfig.EXCHANGE, routingKey);
        } catch (Exception e) {
            // Never fail the main payment flow if RabbitMQ is down
            log.error("Failed to publish event routingKey={}: {}",
                    routingKey, e.getMessage());
        }
    }
}