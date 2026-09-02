package com.nhnacademy.front.dashboard.dto;

import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record DashboardMetricSummaryResponse(
        Long roomId,
        Instant calculatedAt,
        Duration window,
        MetricValue temperature,
        MetricValue humidity,
        MetricValue co2
) {
    private static final String TEMPERATURE = "temperature";
    private static final String HUMIDITY = "humidity";
    private static final String CO2 = "co2";

    public static DashboardMetricSummaryResponse from(SensorMetricSummaryResponse response) {
        List<SensorMetricSummaryResponse.MetricAverage> metrics = response.metrics() == null
                ? List.of()
                : response.metrics();

        return new DashboardMetricSummaryResponse(
                response.roomId(),
                response.calculatedAt(),
                response.window(),
                findMetric(metrics, TEMPERATURE),
                findMetric(metrics, HUMIDITY),
                findMetric(metrics, CO2)
        );
    }

    private static MetricValue findMetric(
            List<SensorMetricSummaryResponse.MetricAverage> metrics,
            String metricCode
    ) {
        return metrics.stream()
                .filter(metric -> metric != null && metricCode.equalsIgnoreCase(metric.metricCode()))
                .findFirst()
                .map(metric -> new MetricValue(metric.averageValue(), metric.unitDisplayName()))
                .orElse(null);
    }

    public record MetricValue(
            Double averageValue,
            String unitDisplayName
    ) {
    }
}
