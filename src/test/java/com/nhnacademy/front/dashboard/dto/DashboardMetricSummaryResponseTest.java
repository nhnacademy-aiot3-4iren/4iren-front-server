package com.nhnacademy.front.dashboard.dto;

import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardMetricSummaryResponseTest {

    @Test
    void extractsTemperatureHumidityAndCo2() {
        SensorMetricSummaryResponse source = summary(List.of(
                metric("temperature", 24.25, "℃"),
                metric("humidity", 51.5, "%"),
                metric("co2", 740.0, "ppm")
        ));

        DashboardMetricSummaryResponse result = DashboardMetricSummaryResponse.from(source);

        assertThat(result.temperature().averageValue()).isEqualTo(24.25);
        assertThat(result.humidity().averageValue()).isEqualTo(51.5);
        assertThat(result.co2().averageValue()).isEqualTo(740.0);
    }

    @Test
    void returnsNullWhenMetricIsMissing() {
        DashboardMetricSummaryResponse result = DashboardMetricSummaryResponse.from(
                summary(List.of(metric("temperature", 20.0, "℃")))
        );

        assertThat(result.humidity()).isNull();
        assertThat(result.co2()).isNull();
    }

    @Test
    void keepsZeroAsAValidAverageValue() {
        DashboardMetricSummaryResponse result = DashboardMetricSummaryResponse.from(
                summary(List.of(metric("temperature", 0.0, "℃")))
        );

        assertThat(result.temperature()).isNotNull();
        assertThat(result.temperature().averageValue()).isZero();
    }

    @Test
    void handlesEmptyMetrics() {
        DashboardMetricSummaryResponse result = DashboardMetricSummaryResponse.from(summary(List.of()));

        assertThat(result.temperature()).isNull();
        assertThat(result.humidity()).isNull();
        assertThat(result.co2()).isNull();
    }

    private SensorMetricSummaryResponse summary(List<SensorMetricSummaryResponse.MetricAverage> metrics) {
        return new SensorMetricSummaryResponse(1L, Instant.parse("2026-09-01T00:00:00Z"), Duration.ofMinutes(15), metrics);
    }

    private SensorMetricSummaryResponse.MetricAverage metric(String code, Double value, String unit) {
        return new SensorMetricSummaryResponse.MetricAverage(
                code, code, "GAUGE", null, value, null, unit, null
        );
    }
}
