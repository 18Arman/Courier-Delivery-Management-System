package com.smartcourier.delivery.dto;

import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliverySummaryResponse(
        Long id,
        String trackingNumber,
        String customerEmail,
        ServiceType serviceType,
        DeliveryStatus status,
        BigDecimal courierCharge,
        LocalDate pickupDate
) {
}

