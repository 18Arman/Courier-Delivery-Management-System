package com.smartcourier.tracking.dto;

import java.time.LocalDateTime;

public record DocumentUploadResponse(
        Long id,
        String trackingNumber,
        String fileName,
        String storedPath,
        LocalDateTime uploadedAt
) {
}

