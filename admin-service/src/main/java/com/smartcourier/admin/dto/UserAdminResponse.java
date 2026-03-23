package com.smartcourier.admin.dto;

public record UserAdminResponse(
        Long id,
        String fullName,
        String email,
        String roleName,
        boolean active
) {
}

