package com.smartcourier.admin.dto;

public record DashboardResponse(
        long activeHubs,
        long activeUsers,
        long openExceptions,
        long availableReports,
        long bookedDeliveries,
        long inTransitDeliveries,
        long deliveredDeliveries,
        long exceptionDeliveries,
        String deliveryServiceState
) {
}
