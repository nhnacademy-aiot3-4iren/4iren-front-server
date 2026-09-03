package com.nhnacademy.front.core.dto.sensor.metric;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record RoomSensorMetricSeriesResponse(
        Long roomId,
        Instant from,
        Instant to,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Duration interval,
        List<SensorSeries> sensors
) {

    public RoomSensorMetricSeriesResponse {
        sensors = List.copyOf(sensors);
    }

    public record SensorSeries(
            String devEui,
            List<MetricSeries> metrics
    ) {

        public SensorSeries {
            metrics = List.copyOf(metrics);
        }
    }

    public record MetricSeries(
            String metricCode,
            String displayName,
            MetricKind metricKind,
            String description,
            String ucumCode,
            String unitDisplayName,
            String symbol,
            List<MetricPoint> points
    ) {

        public MetricSeries {
            points = List.copyOf(points);
        }
    }

    public record MetricPoint(
            Instant bucketEndAt,
            Double averageValue
    ) {
    }
}
