package com.nhnacademy.front.dashboard.client;

import com.nhnacademy.front.dashboard.dto.DashboardChartOptionsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardChartReplaceRequest;
import com.nhnacademy.front.dashboard.dto.DashboardChartResponse;
import com.nhnacademy.front.dashboard.dto.DashboardChartSeriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreDashboardChartClient",
        path = "/api/core/teams"
)
public interface CoreDashboardChartClient {

    @GetMapping("/{teamId}/dashboard/charts")
    List<DashboardChartResponse> getCharts(@PathVariable("teamId") Long teamId);

    @GetMapping("/{teamId}/dashboard/charts/series")
    DashboardChartSeriesResponse getChartSeries(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "clientChartId", required = false) String clientChartId
    );

    @GetMapping("/{teamId}/dashboard/charts/options")
    DashboardChartOptionsResponse getChartOptions(@PathVariable("teamId") Long teamId);

    @PutMapping("/{teamId}/dashboard/charts")
    List<DashboardChartResponse> replaceCharts(
            @PathVariable("teamId") Long teamId,
            @RequestBody DashboardChartReplaceRequest request
    );
}
