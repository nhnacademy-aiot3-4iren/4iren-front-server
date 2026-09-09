package com.nhnacademy.front.dashboard.dto;

public record DashboardChartResponse(
        String clientChartId,
        Long roomId,
        String roomName,
        String buildingName,
        String metricCode,
        String displayName,
        String symbol,
        String timeRange
) {
}
