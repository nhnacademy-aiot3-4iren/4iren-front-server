package com.nhnacademy.front.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "dashboard")
public record DashboardProperties(
        Duration refreshInterval,
        Map<String, MetricThreshold> thresholds
) {

    public DashboardProperties {
        refreshInterval = refreshInterval == null ? Duration.ofSeconds(30) : refreshInterval;
        thresholds = thresholds == null ? Map.of() : Map.copyOf(thresholds);
    }

    public record MetricThreshold(
            Double warningMin,
            Double warningMax,
            Double dangerMin,
            Double dangerMax
    ) {
    }
}
