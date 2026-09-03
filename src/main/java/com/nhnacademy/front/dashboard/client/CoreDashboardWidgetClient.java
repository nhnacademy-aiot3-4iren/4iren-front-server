package com.nhnacademy.front.dashboard.client;

import com.nhnacademy.front.dashboard.dto.DashboardWidgetResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetOptionsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetSeriesResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreDashboardWidgetClient",
        path = "/api/core/teams"
)
public interface CoreDashboardWidgetClient {

    @GetMapping("/{teamId}/dashboard/widgets")
    List<DashboardWidgetResponse> getWidgets(@PathVariable("teamId") Long teamId);

    @GetMapping("/{teamId}/dashboard/widgets/series")
    DashboardWidgetSeriesResponse getWidgetSeries(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "widgetId", required = false) String widgetId
    );

    @GetMapping("/{teamId}/dashboard/widget-options")
    DashboardWidgetOptionsResponse getWidgetOptions(@PathVariable("teamId") Long teamId);

    @PutMapping("/{teamId}/dashboard/widgets")
    List<DashboardWidgetResponse> replaceWidgets(
            @PathVariable("teamId") Long teamId,
            @RequestBody DashboardWidgetUpdateRequest request
    );
}
