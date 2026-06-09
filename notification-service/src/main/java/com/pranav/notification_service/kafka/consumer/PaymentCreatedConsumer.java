package com.pranav.notification_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranav.notification_service.enums.ReferenceType;
import com.pranav.notification_service.kafka.event.PaymentCreatedEvent;
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
public class PaymentCreatedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper        objectMapper;

    @KafkaListener(
            topics   = "payment-created",
            groupId  = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {
        try {
            Object payload = record.value();

            PaymentCreatedEvent event =
                    objectMapper.convertValue(
                            payload,
                            PaymentCreatedEvent.class
                    );

            log.info("Consumed payment-created event: {}",
                    event.getPaymentReference());

            notificationService.sendFromTemplate(
                    "PAYMENT_CREATED",
                    event.getCustomerEmail(),
                    event.getPaymentId().toString(),
                    ReferenceType.PAYMENT,
                    Map.of(
                            "customerName",    event.getCustomerName(),
                            "paymentRef",      event.getPaymentReference(),
                            "amount",          event.getAmount().toPlainString(),
                            "currency",        event.getCurrency()
                    )
            );

        } catch (Exception e) {
            log.error("Error processing payment-created event: {}",
                    e.getMessage(), e);
        }
    }
}