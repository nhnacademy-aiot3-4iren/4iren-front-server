package com.nhnacademy.front.dashboard.dto;

public record DashboardWidgetResponse(
        String id,
        Long roomId,
        String roomName,
        String buildingName,
        String metricCode,
        String displayName,
        String symbol,
        String period
) {
}
