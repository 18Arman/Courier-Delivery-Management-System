package com.smartcourier.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExceptionCaseRequest(
        @NotNull Long deliveryId,
        @NotBlank String trackingNumber,
        @NotBlank String exceptionStatus,
        @NotBlank String issueDescription
) {
}

