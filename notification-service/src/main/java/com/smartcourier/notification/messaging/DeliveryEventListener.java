package com.smartcourier.notification.messaging;

import com.smartcourier.notification.config.MessagingConfig;
import com.smartcourier.notification.integration.DeliveryEventMessage;
import com.smartcourier.notification.service.EmailNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    private final EmailNotificationService emailNotificationService;

    public DeliveryEventListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @RabbitListener(queues = MessagingConfig.NOTIFICATION_QUEUE)
    public void handleDeliveryEvent(DeliveryEventMessage message) {
        emailNotificationService.sendDeliveryNotification(message);
    }
}
