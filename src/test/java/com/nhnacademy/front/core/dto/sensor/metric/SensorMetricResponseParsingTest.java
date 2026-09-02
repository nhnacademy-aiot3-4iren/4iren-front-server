package com.nhnacademy.front.core.dto.sensor.metric;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensorMetricResponseParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void parsesSummaryIncludingZero() throws Exception {
        String json = """
                {
                  "roomId": 7,
                  "calculatedAt": "2026-09-01T00:00:00Z",
                  "window": "PT15M",
                  "metrics": [
                    {"metricCode":"temperature","averageValue":0,"unitDisplayName":"℃"}
                  ]
                }
                """;

        SensorMetricSummaryResponse response = objectMapper.readValue(json, SensorMetricSummaryResponse.class);

        assertThat(response.roomId()).isEqualTo(7L);
        assertThat(response.window()).hasMinutes(15);
        assertThat(response.metrics()).singleElement()
                .extracting(SensorMetricSummaryResponse.MetricAverage::averageValue)
                .isEqualTo(0.0);
    }
}
