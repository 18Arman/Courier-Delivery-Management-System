package com.smartcourier.admin.dto;

import java.time.LocalDateTime;

public record ExceptionCaseResponse(
        Long id,
        Long deliveryId,
        String trackingNumber,
        String exceptionStatus,
        String issueDescription,
        String resolvedBy,
        boolean resolved,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}

