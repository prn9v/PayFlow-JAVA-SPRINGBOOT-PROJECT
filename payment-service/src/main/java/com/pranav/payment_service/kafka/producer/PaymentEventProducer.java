// PaymentEventProducer.java
package com.pranav.payment_service.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranav.payment_service.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-created}")
    private String paymentCreatedTopic;

    @Value("${kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    @Value("${kafka.topics.refund-created}")
    private String refundCreatedTopic;

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        publish(paymentCreatedTopic, event.getPaymentId().toString(), event);
    }

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        publish(paymentSuccessTopic, event.getPaymentId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        publish(paymentFailedTopic, event.getPaymentId().toString(), event);
    }

    public void publishRefundCreated(RefundCreatedEvent event) {
        publish(refundCreatedTopic, event.getPaymentId().toString(), event);
    }

    private void publish(String topic, String key, Object event) {
        try {
            log.info("Publishing to topic: {} | key: {} | payload: {}",
                    topic, key, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize event for logging");
        }
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish to topic {}: {}", topic, ex.getMessage());
                    } else {
                        log.info("Published to topic {} partition {} offset {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}