package com.pranav.notification_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranav.notification_service.enums.ReferenceType;
import com.pranav.notification_service.kafka.event.PaymentFailedEvent;
import com.pranav.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper        objectMapper;

    @KafkaListener(
            topics   = "payment-failed",
            groupId  = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {
        try {
            Object payload = record.value();
            PaymentFailedEvent event = objectMapper.convertValue(
                    payload, PaymentFailedEvent.class);

            log.info("Consumed payment-failed event: {}",
                    event.getPaymentReference());

            notificationService.sendFromTemplate(
                    "PAYMENT_FAILED",
                    event.getCustomerEmail(),
                    event.getPaymentId().toString(),
                    ReferenceType.PAYMENT,
                    Map.of(
                            "paymentRef", event.getPaymentReference(),
                            "amount",     event.getAmount().toPlainString(),
                            "reason",     event.getReason() != null ? event.getReason() : "Unknown"
                    )
            );

        } catch (Exception e) {
            log.error("Error processing payment-failed event: {}",
                    e.getMessage(), e);
        }
    }
}