package com.nhnacademy.front.dashboard.dto;

import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        Long teamId,
        String teamName,
        Instant generatedAt,
        long refreshIntervalSeconds,
        long totalSubscribedRooms,
        List<DashboardMetricDefinitionResponse> availableMetrics,
        List<DashboardRoomResponse> rooms,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public DashboardResponse {
        availableMetrics = List.copyOf(availableMetrics);
        rooms = List.copyOf(rooms);
    }

    public record DashboardRoomResponse(
            Long roomSubscriptionId,
            Long roomId,
            Long buildingId,
            String buildingName,
            String roomName,
            String description,
            long sensorCount,
            boolean notificationEnabled,
            List<DashboardMetricResponse> metrics
    ) {

        public DashboardRoomResponse {
            metrics = List.copyOf(metrics);
        }
    }

    public record DashboardMetricResponse(
            String metricCode,
            String displayName,
            Double value,
            String symbol
    ) {
    }

    public record DashboardMetricDefinitionResponse(
            String metricCode,
            String displayName,
            String symbol
    ) {
    }
}
