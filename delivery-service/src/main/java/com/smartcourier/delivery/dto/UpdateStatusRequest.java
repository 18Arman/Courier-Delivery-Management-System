package com.smartcourier.delivery.dto;

import com.smartcourier.delivery.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull DeliveryStatus status) {
}

