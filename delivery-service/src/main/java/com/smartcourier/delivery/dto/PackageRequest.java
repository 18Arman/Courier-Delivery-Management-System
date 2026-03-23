package com.smartcourier.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PackageRequest(
        @NotBlank String parcelType,
        @NotNull @DecimalMin("0.10") BigDecimal weightInKg,
        @NotNull @DecimalMin("0.00") BigDecimal declaredValue,
        @NotBlank String dimensions,
        String notes
) {
}

