package com.pranav.merchant_service.rabbitmq.producer;

import com.pranav.merchant_service.config.RabbitMQConfig;
import com.pranav.merchant_service.rabbitmq.event.MerchantActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishMerchantActivated(MerchantActivatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_MERCHANT_ACTIVATED,
                    event
            );
            log.info("Published merchant.activated for merchantId={}",
                    event.getMerchantId());
        } catch (Exception e) {
            log.error("Failed to publish merchant.activated: {}",
                    e.getMessage());
        }
    }
}