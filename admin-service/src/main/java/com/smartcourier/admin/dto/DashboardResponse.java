package com.smartcourier.admin.dto;

public record DashboardResponse(
        long activeHubs,
        long activeUsers,
        long openExceptions,
        long availableReports
) {
}

