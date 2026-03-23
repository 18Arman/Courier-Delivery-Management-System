package com.smartcourier.delivery.dto;

import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        String trackingNumber,
        String customerEmail,
        ServiceType serviceType,
        DeliveryStatus status,
        BigDecimal courierCharge,
        LocalDate pickupDate,
        AddressRequest sender,
        AddressRequest receiver,
        PackageRequest parcel,
        LocalDateTime createdAt
) {
}

