package com.smartcourier.tracking.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryProofRequest(
        @NotBlank String trackingNumber,
        @NotBlank String recipientName,
        @NotBlank String proofNote,
        String proofImagePath
) {
}

