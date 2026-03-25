package com.smartcourier.delivery.messaging;

import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.integration.DeliveryEventMessage;
import java.time.LocalDateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public DeliveryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Delivery delivery, String eventType) {
        DeliveryEventMessage message = new DeliveryEventMessage(
                delivery.getId(),
                delivery.getTrackingNumber(),
                delivery.getCustomerEmail(),
                delivery.getServiceType().name(),
                delivery.getStatus().name(),
                eventType,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(MessagingConfig.DELIVERY_EXCHANGE, MessagingConfig.DELIVERY_ROUTING_KEY, message);
    }
}

