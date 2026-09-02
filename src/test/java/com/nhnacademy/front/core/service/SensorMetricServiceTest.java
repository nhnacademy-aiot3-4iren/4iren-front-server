package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreSensorMetricClient;
import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensorMetricServiceTest {

    private final CoreSensorMetricClient client = mock(CoreSensorMetricClient.class);
    private final SensorMetricService service = new SensorMetricService(client);

    @Test
    void delegatesSummaryRequest() {
        SensorMetricSummaryResponse expected = new SensorMetricSummaryResponse(
                3L, Instant.parse("2026-09-01T00:00:00Z"), Duration.ofMinutes(15), List.of()
        );
        when(client.getSummary(1L, 3L)).thenReturn(expected);

        assertThat(service.getSummary(1L, 3L)).isSameAs(expected);
    }
}
