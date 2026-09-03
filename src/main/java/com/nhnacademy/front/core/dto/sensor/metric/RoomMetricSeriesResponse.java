package com.nhnacademy.front.core.dto.sensor.metric;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record RoomMetricSeriesResponse(
        Long roomId,
        String metricCode,
        String displayName,
        MetricKind metricKind,
        String description,
        String ucumCode,
        String unitDisplayName,
        String symbol,
        Instant from,
        Instant to,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Duration interval,
        List<MetricPoint> points
) {

    public RoomMetricSeriesResponse {
        points = List.copyOf(points);
    }

    public record MetricPoint(
            Instant bucketEndAt,
            Double averageValue
    ) {
    }
}
