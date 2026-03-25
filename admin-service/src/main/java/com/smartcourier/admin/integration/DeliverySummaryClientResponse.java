package com.smartcourier.admin.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliverySummaryClientResponse(
        Long id,
        String trackingNumber,
        String customerEmail,
        String serviceType,
        String status,
        BigDecimal courierCharge,
        LocalDate pickupDate
) {
}

