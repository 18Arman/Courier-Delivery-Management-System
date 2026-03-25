package com.smartcourier.admin.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "delivery-service", path = "/api/v1/internal/deliveries")
public interface DeliveryServiceClient {

    @GetMapping("/stats")
    DeliveryStatsClientResponse fetchStats();

    @GetMapping("/{id}")
    DeliverySummaryClientResponse fetchDeliverySummary(@PathVariable("id") Long deliveryId);
}
