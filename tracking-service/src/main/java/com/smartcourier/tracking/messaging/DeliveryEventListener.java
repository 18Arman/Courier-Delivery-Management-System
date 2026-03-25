package com.smartcourier.tracking.messaging;

import com.smartcourier.tracking.integration.DeliveryEventMessage;
import com.smartcourier.tracking.service.TrackingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    private final TrackingService trackingService;

    public DeliveryEventListener(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @RabbitListener(queues = MessagingConfig.TRACKING_QUEUE)
    public void handleDeliveryEvent(DeliveryEventMessage message) {
        trackingService.syncFromDeliveryEvent(message);
    }
}

