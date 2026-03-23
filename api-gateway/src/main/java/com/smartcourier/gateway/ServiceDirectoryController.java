package com.smartcourier.gateway;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class ServiceDirectoryController {

    @GetMapping("/services")
    public ResponseEntity<List<Map<String, String>>> services() {
        return ResponseEntity.ok(List.of(
                Map.of("name", "Auth Service", "path", "/gateway/auth", "description", "Signup, login, JWT issuance"),
                Map.of("name", "Delivery Service", "path", "/gateway/deliveries", "description", "Booking and lifecycle management"),
                Map.of("name", "Tracking Service", "path", "/gateway/tracking", "description", "Tracking events, documents, proof"),
                Map.of("name", "Admin Service", "path", "/gateway/admin", "description", "Dashboard, reports, hubs, issue resolution")
        ));
    }
}

