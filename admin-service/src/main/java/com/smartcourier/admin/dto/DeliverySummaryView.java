package com.smartcourier.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliverySummaryView(
        Long id,
        String trackingNumber,
        String customerEmail,
        String serviceType,
        String status,
        BigDecimal courierCharge,
        LocalDate pickupDate
) {
}

