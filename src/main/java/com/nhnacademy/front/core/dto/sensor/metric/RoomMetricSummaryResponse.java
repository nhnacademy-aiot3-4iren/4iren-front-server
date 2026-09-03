package com.nhnacademy.front.core.dto.sensor.metric;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record RoomMetricSummaryResponse(
        Long roomId,
        Instant calculatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Duration window,
        List<MetricAverage> metrics
) {

    public RoomMetricSummaryResponse {
        metrics = List.copyOf(metrics);
    }

    public record MetricAverage(
            String metricCode,
            String displayName,
            MetricKind metricKind,
            String description,
            Double averageValue,
            String ucumCode,
            String unitDisplayName,
            String symbol
    ) {
    }
}
