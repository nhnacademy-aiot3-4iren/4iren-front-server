package com.nhnacademy.front.dashboard.service;

import com.nhnacademy.front.dashboard.client.CoreDashboardWidgetClient;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetOptionsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetSeriesResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardWidgetService {

    private final CoreDashboardWidgetClient dashboardWidgetClient;

    public List<DashboardWidgetResponse> getWidgets(Long teamId) {
        return dashboardWidgetClient.getWidgets(teamId);
    }

    public DashboardWidgetSeriesResponse getWidgetSeries(Long teamId) {
        return getWidgetSeries(teamId, null);
    }

    public DashboardWidgetSeriesResponse getWidgetSeries(Long teamId, String widgetId) {
        return dashboardWidgetClient.getWidgetSeries(teamId, widgetId);
    }

    public DashboardWidgetOptionsResponse getWidgetOptions(Long teamId) {
        return dashboardWidgetClient.getWidgetOptions(teamId);
    }

    public List<DashboardWidgetResponse> replaceWidgets(
            Long teamId,
            DashboardWidgetUpdateRequest request
    ) {
        return dashboardWidgetClient.replaceWidgets(teamId, request);
    }
}
