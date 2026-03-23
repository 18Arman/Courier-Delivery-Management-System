package com.smartcourier.admin.dto;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        String reportName,
        String reportType,
        String summary,
        LocalDateTime generatedAt
) {
}

