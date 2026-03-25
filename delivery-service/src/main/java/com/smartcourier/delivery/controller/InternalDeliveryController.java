package com.smartcourier.delivery.controller;

import com.smartcourier.delivery.dto.DeliveryStatsResponse;
import com.smartcourier.delivery.dto.DeliverySummaryResponse;
import com.smartcourier.delivery.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/deliveries")
public class InternalDeliveryController {

    private final DeliveryService deliveryService;

    public InternalDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliverySummaryResponse> getSummary(@PathVariable("id") Long id) {
        return ResponseEntity.ok(deliveryService.getSummaryById(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<DeliveryStatsResponse> getStats() {
        return ResponseEntity.ok(deliveryService.getStats());
    }
}
