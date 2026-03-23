package com.smartcourier.tracking.dto;

import java.time.LocalDateTime;

public record TrackingEventResponse(
        Long id,
        String trackingNumber,
        String status,
        String location,
        String description,
        LocalDateTime eventTime
) {
}

