package com.pranav.merchant_service.kafka.producer;

import com.pranav.merchant_service.kafka.event.MerchantActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.merchant-activated}")
    private String merchantActivatedTopic;

    public void publishMerchantActivated(MerchantActivatedEvent event) {
        kafkaTemplate.send(
                merchantActivatedTopic,
                event.getMerchantId().toString(),
                event
        );
        log.info("Published merchant-activated event for merchantId: {}",
                event.getMerchantId());
    }
}