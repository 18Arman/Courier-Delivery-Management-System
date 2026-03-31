package com.smartcourier.notification.service;

import com.smartcourier.notification.integration.DeliveryEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${app.mail.from:no-reply@smartcourier.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendDeliveryNotification(DeliveryEventMessage message) {
        if (message.getCustomerEmail() == null || message.getCustomerEmail().isBlank()) {
            log.warn("Skipping email notification because customer email is missing for delivery {}", message.getDeliveryId());
            return;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(message.getCustomerEmail());
        mailMessage.setSubject(buildSubject(message));
        mailMessage.setText(buildBody(message));

        mailSender.send(mailMessage);
        log.info("Email notification sent to {} for delivery {}", message.getCustomerEmail(), message.getDeliveryId());
    }

    private String buildSubject(DeliveryEventMessage message) {
        if ("DELIVERY_CREATED".equalsIgnoreCase(message.getEventType())) {
            return "SmartCourier booking confirmed - " + message.getTrackingNumber();
        }
        return "SmartCourier delivery update - " + message.getTrackingNumber();
    }

    private String buildBody(DeliveryEventMessage message) {
        return """
                Hello,

                Your SmartCourier delivery has been updated.

                Tracking Number: %s
                Delivery ID: %s
                Service Type: %s
                Current Status: %s
                Event Type: %s
                Event Time: %s

                You can use this tracking number in the SmartCourier system to follow your parcel.

                Regards,
                SmartCourier
                """.formatted(
                message.getTrackingNumber(),
                message.getDeliveryId(),
                message.getServiceType(),
                message.getStatus(),
                message.getEventType(),
                message.getOccurredAt()
        );
    }
}
