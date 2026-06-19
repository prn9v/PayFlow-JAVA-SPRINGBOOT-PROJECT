package com.pranav.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange ──────────────────────────────────────────────────────────────
    public static final String EXCHANGE = "payflow.events";

    // ── Routing Keys ──────────────────────────────────────────────────────────
    public static final String KEY_PAYMENT_CREATED    = "payment.created";
    public static final String KEY_PAYMENT_SUCCESS    = "payment.success";
    public static final String KEY_PAYMENT_FAILED     = "payment.failed";
    public static final String KEY_REFUND_CREATED     = "refund.created";
    public static final String KEY_MERCHANT_ACTIVATED = "merchant.activated";

    // ── Queue Names ───────────────────────────────────────────────────────────
    public static final String QUEUE_PAYMENT_CREATED    = "queue.payment.created";
    public static final String QUEUE_PAYMENT_SUCCESS    = "queue.payment.success";
    public static final String QUEUE_PAYMENT_FAILED     = "queue.payment.failed";
    public static final String QUEUE_REFUND_CREATED     = "queue.refund.created";
    public static final String QUEUE_MERCHANT_ACTIVATED = "queue.merchant.activated";

    // ── Exchange Bean ─────────────────────────────────────────────────────────
    @Bean
    public TopicExchange payflowExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Queues ────────────────────────────────────────────────────────────────
    @Bean public Queue paymentCreatedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_CREATED).build();
    }

    @Bean public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_SUCCESS).build();
    }

    @Bean public Queue paymentFailedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_FAILED).build();
    }

    @Bean public Queue refundCreatedQueue() {
        return QueueBuilder.durable(QUEUE_REFUND_CREATED).build();
    }

    @Bean public Queue merchantActivatedQueue() {
        return QueueBuilder.durable(QUEUE_MERCHANT_ACTIVATED).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────
    @Bean
    public Binding bindPaymentCreated(
            Queue paymentCreatedQueue, TopicExchange payflowExchange) {
        return BindingBuilder
                .bind(paymentCreatedQueue)
                .to(payflowExchange)
                .with(KEY_PAYMENT_CREATED);
    }

    @Bean
    public Binding bindPaymentSuccess(
            Queue paymentSuccessQueue, TopicExchange payflowExchange) {
        return BindingBuilder
                .bind(paymentSuccessQueue)
                .to(payflowExchange)
                .with(KEY_PAYMENT_SUCCESS);
    }

    @Bean
    public Binding bindPaymentFailed(
            Queue paymentFailedQueue, TopicExchange payflowExchange) {
        return BindingBuilder
                .bind(paymentFailedQueue)
                .to(payflowExchange)
                .with(KEY_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindRefundCreated(
            Queue refundCreatedQueue, TopicExchange payflowExchange) {
        return BindingBuilder
                .bind(refundCreatedQueue)
                .to(payflowExchange)
                .with(KEY_REFUND_CREATED);
    }

    @Bean
    public Binding bindMerchantActivated(
            Queue merchantActivatedQueue, TopicExchange payflowExchange) {
        return BindingBuilder
                .bind(merchantActivatedQueue)
                .to(payflowExchange)
                .with(KEY_MERCHANT_ACTIVATED);
    }

    // ── JSON Message Converter ────────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── RabbitTemplate with JSON ──────────────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ── Listener factory — low memory settings ────────────────────────────────
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(1);    // ← 1 thread (low memory)
        factory.setMaxConcurrentConsumers(1); // ← never scale up
        factory.setPrefetchCount(1);          // ← process 1 msg at a time
        return factory;
    }
}