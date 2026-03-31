package com.smartcourier.delivery.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    public static final String DELIVERY_EXCHANGE = "smartcourier.delivery.exchange";
    public static final String DELIVERY_ROUTING_KEY = "delivery.events";
    public static final String TRACKING_QUEUE = "smartcourier.tracking.delivery-events";
    public static final String ADMIN_QUEUE = "smartcourier.admin.delivery-events";
    public static final String NOTIFICATION_QUEUE = "smartcourier.notification.delivery-events";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Queue trackingDeliveryQueue() {
        return new Queue(TRACKING_QUEUE, true);
    }

    @Bean
    public Queue adminDeliveryQueue() {
        return new Queue(ADMIN_QUEUE, true);
    }

    @Bean
    public Queue notificationDeliveryQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding trackingBinding(@Qualifier("trackingDeliveryQueue") Queue trackingDeliveryQueue,
                                   TopicExchange deliveryExchange) {
        return BindingBuilder.bind(trackingDeliveryQueue).to(deliveryExchange).with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding adminBinding(@Qualifier("adminDeliveryQueue") Queue adminDeliveryQueue,
                                TopicExchange deliveryExchange) {
        return BindingBuilder.bind(adminDeliveryQueue).to(deliveryExchange).with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(@Qualifier("notificationDeliveryQueue") Queue notificationDeliveryQueue,
                                       TopicExchange deliveryExchange) {
        return BindingBuilder.bind(notificationDeliveryQueue).to(deliveryExchange).with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
