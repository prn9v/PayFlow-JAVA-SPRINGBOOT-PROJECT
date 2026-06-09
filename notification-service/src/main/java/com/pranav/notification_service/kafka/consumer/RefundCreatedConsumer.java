package com.pranav.notification_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranav.notification_service.enums.ReferenceType;
import com.pranav.notification_service.kafka.event.RefundCreatedEvent;
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
public class RefundCreatedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper        objectMapper;

    @KafkaListener(
            topics   = "refund-created",
            groupId  = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {
        try {
            Object payload = record.value();
            RefundCreatedEvent event = objectMapper.convertValue(
                    payload, RefundCreatedEvent.class);

            log.info("Consumed refund-created event: {}",
                    event.getRefundReference());

            notificationService.sendFromTemplate(
                    "REFUND_CREATED",
                    event.getCustomerEmail(),
                    event.getPaymentId().toString(),
                    ReferenceType.REFUND,
                    Map.of(
                            "refundRef", event.getRefundReference(),
                            "amount",    event.getAmount().toPlainString()
                    )
            );

        } catch (Exception e) {
            log.error("Error processing refund-created event: {}",
                    e.getMessage(), e);
        }
    }
}