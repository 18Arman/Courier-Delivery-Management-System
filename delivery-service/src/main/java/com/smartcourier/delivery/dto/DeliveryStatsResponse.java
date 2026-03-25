package com.smartcourier.delivery.dto;

public record DeliveryStatsResponse(
        long bookedCount,
        long inTransitCount,
        long deliveredCount,
        long exceptionCount
) {
}

