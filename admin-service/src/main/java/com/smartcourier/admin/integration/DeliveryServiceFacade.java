package com.smartcourier.admin.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class DeliveryServiceFacade {

    private final DeliveryServiceClient deliveryServiceClient;

    public DeliveryServiceFacade(DeliveryServiceClient deliveryServiceClient) {
        this.deliveryServiceClient = deliveryServiceClient;
    }

    @CircuitBreaker(name = "deliveryService", fallbackMethod = "fetchStatsFallback")
    public DeliveryStatsClientResponse fetchStats() {
        return deliveryServiceClient.fetchStats();
    }

    @CircuitBreaker(name = "deliveryService", fallbackMethod = "fetchDeliverySummaryFallback")
    public DeliverySummaryClientResponse fetchDeliverySummary(Long deliveryId) {
        return deliveryServiceClient.fetchDeliverySummary(deliveryId);
    }

    public boolean isHealthy() {
        DeliveryStatsClientResponse response = fetchStats();
        return response.bookedCount() >= 0
                && response.inTransitCount() >= 0
                && response.deliveredCount() >= 0
                && response.exceptionCount() >= 0;
    }

    @SuppressWarnings("unused")
    private DeliveryStatsClientResponse fetchStatsFallback(Throwable throwable) {
        return new DeliveryStatsClientResponse(-1, -1, -1, -1);
    }

    @SuppressWarnings("unused")
    private DeliverySummaryClientResponse fetchDeliverySummaryFallback(Long deliveryId, Throwable throwable) {
        return new DeliverySummaryClientResponse(deliveryId, "UNAVAILABLE", "unknown", "UNKNOWN", "UNKNOWN", null, null);
    }
}
