package com.nhnacademy.front.dashboard.service;

import com.nhnacademy.front.dashboard.client.CoreDashboardSnapshotClient;
import com.nhnacademy.front.dashboard.config.DashboardProperties;
import com.nhnacademy.front.dashboard.dto.CoreDashboardSnapshotResponse;
import com.nhnacademy.front.dashboard.dto.DashboardResponse;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsRequest;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardSubscriptionCandidatesResponse;
import com.nhnacademy.front.dashboard.dto.DashboardResponse.DashboardMetricDefinitionResponse;
import com.nhnacademy.front.dashboard.dto.DashboardResponse.DashboardMetricResponse;
import com.nhnacademy.front.dashboard.dto.DashboardResponse.DashboardRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CoreDashboardSnapshotClient dashboardSnapshotClient;
    private final DashboardProperties properties;

    public DashboardResponse getDashboard(
            Long teamId,
            int page,
            int size,
            String query,
            List<String> metricCodes
    ) {
        CoreDashboardSnapshotResponse snapshot = dashboardSnapshotClient.getSnapshot(
                teamId,
                page,
                size,
                query,
                metricCodes
        );

        return new DashboardResponse(
                snapshot.teamId(),
                snapshot.teamName(),
                snapshot.generatedAt(),
                properties.refreshInterval().toSeconds(),
                snapshot.totalSubscribedRooms(),
                snapshot.availableMetrics().stream()
                        .map(metric -> new DashboardMetricDefinitionResponse(
                                metric.metricCode(),
                                metric.displayName(),
                                metric.symbol()
                        ))
                        .toList(),
                snapshot.rooms().stream()
                        .map(room -> new DashboardRoomResponse(
                                room.roomSubscriptionId(),
                                room.roomId(),
                                room.buildingId(),
                                room.buildingName(),
                                room.roomName(),
                                room.description(),
                                room.sensorCount(),
                                room.notificationEnabled(),
                                room.metrics().stream()
                                        .map(metric -> new DashboardMetricResponse(
                                                metric.metricCode(),
                                                metric.displayName(),
                                                metric.value(),
                                                metric.symbol()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                snapshot.page(),
                snapshot.size(),
                snapshot.totalElements(),
                snapshot.totalPages(),
                snapshot.first(),
                snapshot.last()
        );
    }

    public DashboardRoomMetricsResponse getRoomMetrics(
            Long teamId,
            DashboardRoomMetricsRequest request
    ) {
        return dashboardSnapshotClient.getRoomMetrics(teamId, request);
    }

    public DashboardSubscriptionCandidatesResponse getSubscriptionCandidates(
            Long teamId,
            String query,
            int page,
            int size
    ) {
        return dashboardSnapshotClient.getSubscriptionCandidates(
                teamId,
                query,
                page,
                size
        );
    }
}
