package com.smartcourier.tracking.dto;

import java.time.LocalDateTime;

public record DeliveryProofResponse(
        Long id,
        String trackingNumber,
        String recipientName,
        String proofNote,
        String proofImagePath,
        LocalDateTime deliveredAt
) {
}

