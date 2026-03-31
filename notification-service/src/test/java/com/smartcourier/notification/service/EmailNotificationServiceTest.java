package com.smartcourier.notification.service;

import com.smartcourier.notification.integration.DeliveryEventMessage;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmailNotificationServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailNotificationService service = new EmailNotificationService(mailSender, "no-reply@smartcourier.local");

    @Test
    void shouldSendDeliveryCreatedEmail() {
        DeliveryEventMessage message = new DeliveryEventMessage();
        message.setDeliveryId(1L);
        message.setTrackingNumber("SC20260331120000");
        message.setCustomerEmail("customer@example.com");
        message.setServiceType("EXPRESS");
        message.setStatus("BOOKED");
        message.setEventType("DELIVERY_CREATED");
        message.setOccurredAt(LocalDateTime.now());

        service.sendDeliveryNotification(message);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSkipWhenEmailMissing() {
        DeliveryEventMessage message = new DeliveryEventMessage();
        message.setDeliveryId(1L);

        service.sendDeliveryNotification(message);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
