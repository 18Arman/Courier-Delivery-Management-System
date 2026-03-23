package com.smartcourier.admin.dto;

public record HubResponse(
        Long id,
        String hubCode,
        String city,
        String state,
        String managerName,
        boolean active
) {
}

