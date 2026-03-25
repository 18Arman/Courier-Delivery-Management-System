package com.smartcourier.delivery.integration;

import java.time.LocalDateTime;

public class DeliveryEventMessage {

    private Long deliveryId;
    private String trackingNumber;
    private String customerEmail;
    private String serviceType;
    private String status;
    private String eventType;
    private LocalDateTime occurredAt;

    public DeliveryEventMessage() {
    }

    public DeliveryEventMessage(Long deliveryId, String trackingNumber, String customerEmail, String serviceType,
                                String status, String eventType, LocalDateTime occurredAt) {
        this.deliveryId = deliveryId;
        this.trackingNumber = trackingNumber;
        this.customerEmail = customerEmail;
        this.serviceType = serviceType;
        this.status = status;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}

