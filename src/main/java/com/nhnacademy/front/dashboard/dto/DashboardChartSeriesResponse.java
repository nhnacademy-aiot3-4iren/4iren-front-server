package com.nhnacademy.front.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record DashboardChartSeriesResponse(
        Instant generatedAt,
        List<ChartSeries> charts
) {
    public DashboardChartSeriesResponse {
        charts = List.copyOf(charts);
    }

    public record ChartSeries(
            String clientChartId,
            Long roomId,
            String roomName,
            String buildingName,
            String metricCode,
            String displayName,
            String symbol,
            String timeRange,
            Instant from,
            Instant to,
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Duration interval,
            String errorCode,
            List<MetricPoint> points
    ) {
        public ChartSeries {
            points = List.copyOf(points);
        }
    }

    public record MetricPoint(
            Instant bucketEndAt,
            Double averageValue,
            boolean partial
    ) {
    }
}
