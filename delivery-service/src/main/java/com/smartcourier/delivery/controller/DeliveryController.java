package com.smartcourier.delivery.controller;

import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.dto.UpdateStatusRequest;
import com.smartcourier.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Create a courier delivery request")
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody CreateDeliveryRequest request,
                                                   org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.create(authentication.getName(), request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Fetch deliveries created by the current user")
    public ResponseEntity<List<DeliveryResponse>> myDeliveries(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get delivery details by delivery id")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id,
                                                    org.springframework.security.core.Authentication authentication) {
        UserDetails userDetails = User.withUsername(authentication.getName()).password("")
                .authorities(authentication.getAuthorities()).build();
        return ResponseEntity.ok(deliveryService.getById(id, authentication.getName(), deliveryService.isAdmin(userDetails)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Advance or resolve the delivery lifecycle")
    public ResponseEntity<DeliveryResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request.status()));
    }
}
