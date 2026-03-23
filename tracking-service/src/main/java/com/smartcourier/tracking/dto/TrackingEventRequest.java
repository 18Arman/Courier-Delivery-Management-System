package com.smartcourier.tracking.dto;

import jakarta.validation.constraints.NotBlank;

public record TrackingEventRequest(
        @NotBlank String trackingNumber,
        @NotBlank String status,
        @NotBlank String location,
        @NotBlank String description
) {
}

