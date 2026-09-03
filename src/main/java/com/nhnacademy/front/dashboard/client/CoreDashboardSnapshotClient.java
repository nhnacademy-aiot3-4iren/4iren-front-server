package com.nhnacademy.front.dashboard.client;

import com.nhnacademy.front.dashboard.dto.CoreDashboardSnapshotResponse;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsRequest;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardSubscriptionCandidatesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreDashboardSnapshotClient",
        path = "/api/core/teams"
)
public interface CoreDashboardSnapshotClient {

    @GetMapping("/{teamId}/dashboard/snapshot")
    CoreDashboardSnapshotResponse getSnapshot(
            @PathVariable("teamId") Long teamId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("query") String query,
            @RequestParam(name = "metricCode", required = false) List<String> metricCodes
    );

    @PostMapping("/{teamId}/dashboard/room-metrics")
    DashboardRoomMetricsResponse getRoomMetrics(
            @PathVariable("teamId") Long teamId,
            @RequestBody DashboardRoomMetricsRequest request
    );

    @GetMapping("/{teamId}/dashboard/subscription-candidates")
    DashboardSubscriptionCandidatesResponse getSubscriptionCandidates(
            @PathVariable("teamId") Long teamId,
            @RequestParam("query") String query,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
