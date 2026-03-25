package com.smartcourier.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 3, max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank
        @Pattern(regexp = "^(\\+91)?[6-9][0-9]{9}$",message="It must have 10 digits starting with 6-9 digit only!!")
        String phoneNumber
) {
}

