package com.nhnacademy.front.core.dto.sensor.metric;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record SensorMetricSummaryResponse(
        Long roomId,
        Instant calculatedAt,
        Duration window,
        List<MetricAverage> metrics
) {
    public record MetricAverage(
            String metricCode,
            String displayName,
            String metricKind,
            String description,
            Double averageValue,
            String ucumCode,
            String unitDisplayName,
            String symbol
    ) {
    }
}
