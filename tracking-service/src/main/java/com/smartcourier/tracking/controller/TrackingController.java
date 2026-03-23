package com.smartcourier.tracking.controller;

import com.smartcourier.tracking.dto.DeliveryProofRequest;
import com.smartcourier.tracking.dto.DeliveryProofResponse;
import com.smartcourier.tracking.dto.DocumentUploadResponse;
import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.dto.TrackingEventResponse;
import com.smartcourier.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Track a parcel by tracking number")
    public ResponseEntity<List<TrackingEventResponse>> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getTimeline(trackingNumber));
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a tracking event for a parcel")
    public ResponseEntity<TrackingEventResponse> addEvent(@Valid @RequestBody TrackingEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackingService.addEvent(request));
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Upload parcel-related documents")
    public ResponseEntity<DocumentUploadResponse> upload(@RequestParam String trackingNumber,
                                                         @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackingService.uploadDocument(trackingNumber, file));
    }

    @PutMapping("/proof")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Store proof of delivery details")
    public ResponseEntity<DeliveryProofResponse> saveProof(@Valid @RequestBody DeliveryProofRequest request) {
        return ResponseEntity.ok(trackingService.saveProof(request));
    }

    @GetMapping("/{trackingNumber}/proof")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Fetch delivery proof")
    public ResponseEntity<DeliveryProofResponse> getProof(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getProof(trackingNumber));
    }
}
