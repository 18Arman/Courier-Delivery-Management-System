package com.smartcourier.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveExceptionRequest(@NotBlank String resolvedBy) {
}

