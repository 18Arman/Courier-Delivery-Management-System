package com.smartcourier.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserAdminRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String roleName,
        boolean active
) {
}

