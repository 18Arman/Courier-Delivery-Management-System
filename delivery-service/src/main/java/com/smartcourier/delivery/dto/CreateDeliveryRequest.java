package com.smartcourier.delivery.dto;

import com.smartcourier.delivery.entity.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateDeliveryRequest(
        @NotNull ServiceType serviceType,
        @Valid @NotNull AddressRequest sender,
        @Valid @NotNull AddressRequest receiver,
        @Valid @NotNull PackageRequest parcel,
        @FutureOrPresent LocalDate pickupDate
) {
}

