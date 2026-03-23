package com.smartcourier.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record HubRequest(
        @NotBlank String hubCode,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String managerName,
        boolean active
) {
}

