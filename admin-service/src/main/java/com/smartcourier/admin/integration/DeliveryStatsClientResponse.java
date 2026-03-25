package com.smartcourier.admin.integration;

public record DeliveryStatsClientResponse(
        long bookedCount,
        long inTransitCount,
        long deliveredCount,
        long exceptionCount
) {
}

