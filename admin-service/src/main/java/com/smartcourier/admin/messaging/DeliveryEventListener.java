package com.smartcourier.admin.messaging;

import com.smartcourier.admin.integration.DeliveryEventMessage;
import com.smartcourier.admin.service.AdminService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    private final AdminService adminService;

    public DeliveryEventListener(AdminService adminService) {
        this.adminService = adminService;
    }

    @RabbitListener(queues = MessagingConfig.ADMIN_QUEUE)
    public void handleDeliveryEvent(DeliveryEventMessage message) {
        adminService.processDeliveryEvent(message);
    }
}

