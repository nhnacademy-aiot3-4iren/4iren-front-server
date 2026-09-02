package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;
import com.nhnacademy.front.core.service.SensorMetricService;
import com.nhnacademy.front.dashboard.dto.DashboardRoomResponse;
import com.nhnacademy.front.dashboard.service.DashboardRoomService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardRestControllerTest {

    private final SensorMetricService sensorMetricService = mock(SensorMetricService.class);
    private final DashboardRoomService dashboardRoomService = mock(DashboardRoomService.class);
    private final DashboardRestController controller = new DashboardRestController(
            sensorMetricService, dashboardRoomService
    );

    @Test
    void returnsCurrentUsersSubscribedRooms() {
        List<DashboardRoomResponse> rooms = List.of(
                new DashboardRoomResponse(1L, 10L, 20L, "본관", "회의실")
        );
        when(dashboardRoomService.getSubscribedRooms(7L)).thenReturn(rooms);

        assertThat(controller.getDashboardRooms(7L)).isSameAs(rooms);
        verify(dashboardRoomService).getSubscribedRooms(7L);
    }

    @Test
    void roomChangeUsesTheNewRoomIdForSummary() {
        when(sensorMetricService.getSummary(1L, 10L)).thenReturn(summary(10L));
        when(sensorMetricService.getSummary(1L, 11L)).thenReturn(summary(11L));

        assertThat(controller.getSummary(1L, 10L).roomId()).isEqualTo(10L);
        assertThat(controller.getSummary(1L, 11L).roomId()).isEqualTo(11L);
        verify(sensorMetricService).getSummary(1L, 10L);
        verify(sensorMetricService).getSummary(1L, 11L);
    }
    private SensorMetricSummaryResponse summary(Long roomId) {
        return new SensorMetricSummaryResponse(
                roomId, Instant.parse("2026-09-01T00:00:00Z"), Duration.ofMinutes(15), List.of()
        );
    }
}
