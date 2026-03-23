package com.smartcourier.auth.dto;

import java.util.Set;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        boolean active,
        Set<String> roles
) {
}

