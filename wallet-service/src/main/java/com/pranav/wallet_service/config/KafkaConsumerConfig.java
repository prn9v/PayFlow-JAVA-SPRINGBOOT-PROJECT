package com.pranav.wallet_service.config;

import com.pranav.wallet_service.kafka.event.MerchantActivatedEvent;
import com.pranav.wallet_service.kafka.event.PaymentSuccessEvent;
import com.pranav.wallet_service.kafka.event.RefundCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ── One factory per event type ────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, MerchantActivatedEvent>
    merchantActivatedConsumerFactory() {
        return buildConsumerFactory(MerchantActivatedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessEvent>
    paymentSuccessConsumerFactory() {
        return buildConsumerFactory(PaymentSuccessEvent.class);
    }

    @Bean
    public ConsumerFactory<String, RefundCreatedEvent>
    refundCreatedConsumerFactory() {
        return buildConsumerFactory(RefundCreatedEvent.class);
    }

    // ── One container factory per event type ──────────────────────────────────

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MerchantActivatedEvent>
    kafkaListenerContainerFactory() {
        // This is the DEFAULT factory — used by MerchantActivatedConsumer
        ConcurrentKafkaListenerContainerFactory<String, MerchantActivatedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(merchantActivatedConsumerFactory());
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(1000L, 2L)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent>
    paymentSuccessContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentSuccessConsumerFactory());
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(1000L, 2L)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundCreatedEvent>
    refundCreatedContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RefundCreatedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(refundCreatedConsumerFactory());
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(1000L, 2L)));
        return factory;
    }

    // ── Generic builder ───────────────────────────────────────────────────────

    private <T> ConsumerFactory<String, T> buildConsumerFactory(
            Class<T> targetType) {

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,           "wallet-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,  "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }
}