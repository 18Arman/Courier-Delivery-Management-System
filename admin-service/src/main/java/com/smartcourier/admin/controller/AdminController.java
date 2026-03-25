package com.smartcourier.admin.controller;

import com.smartcourier.admin.dto.DashboardResponse;
import com.smartcourier.admin.dto.DeliverySummaryView;
import com.smartcourier.admin.dto.ExceptionCaseRequest;
import com.smartcourier.admin.dto.ExceptionCaseResponse;
import com.smartcourier.admin.dto.HubRequest;
import com.smartcourier.admin.dto.HubResponse;
import com.smartcourier.admin.dto.ReportResponse;
import com.smartcourier.admin.dto.ResolveExceptionRequest;
import com.smartcourier.admin.dto.UserAdminRequest;
import com.smartcourier.admin.dto.UserAdminResponse;
import com.smartcourier.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard overview")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Fetch unresolved delivery exceptions")
    public ResponseEntity<List<ExceptionCaseResponse>> deliveries() {
        return ResponseEntity.ok(adminService.getOpenExceptions());
    }

    @GetMapping("/deliveries/{deliveryId}/overview")
    @Operation(summary = "Synchronously fetch live delivery details from delivery service")
    public ResponseEntity<DeliverySummaryView> deliveryOverview(@PathVariable("deliveryId") Long deliveryId) {
        return ResponseEntity.ok(adminService.getDeliveryOverview(deliveryId));
    }

    @PostMapping("/deliveries")
    @Operation(summary = "Create an exception case for delivery monitoring")
    public ResponseEntity<ExceptionCaseResponse> createException(@Valid @RequestBody ExceptionCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createExceptionCase(request));
    }

    @PutMapping("/deliveries/{id}/resolve")
    @Operation(summary = "Resolve a delayed, failed, or returned delivery")
    public ResponseEntity<ExceptionCaseResponse> resolve(@PathVariable("id") Long id,
                                                         @Valid @RequestBody ResolveExceptionRequest request) {
        return ResponseEntity.ok(adminService.resolveException(id, request.resolvedBy()));
    }

    @GetMapping("/reports")
    @Operation(summary = "Fetch generated reports")
    public ResponseEntity<List<ReportResponse>> reports() {
        return ResponseEntity.ok(adminService.getReports());
    }

    @PostMapping("/reports")
    @Operation(summary = "Generate an operational report snapshot")
    public ResponseEntity<ReportResponse> generateReport() {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.generateOperationalReport());
    }

    @GetMapping("/users")
    @Operation(summary = "List users managed by admin service")
    public ResponseEntity<List<UserAdminResponse>> users() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @PostMapping("/users")
    @Operation(summary = "Register a managed user snapshot")
    public ResponseEntity<UserAdminResponse> createUser(@Valid @RequestBody UserAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/hubs")
    @Operation(summary = "List courier hubs")
    public ResponseEntity<List<HubResponse>> hubs() {
        return ResponseEntity.ok(adminService.getHubs());
    }

    @PostMapping("/hubs")
    @Operation(summary = "Create a courier hub")
    public ResponseEntity<HubResponse> createHub(@Valid @RequestBody HubRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createHub(request));
    }
}
