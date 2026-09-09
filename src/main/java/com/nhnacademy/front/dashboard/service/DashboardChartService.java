package com.nhnacademy.front.dashboard.service;

import com.nhnacademy.front.dashboard.client.CoreDashboardChartClient;
import com.nhnacademy.front.dashboard.dto.DashboardChartOptionsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardChartReplaceRequest;
import com.nhnacademy.front.dashboard.dto.DashboardChartResponse;
import com.nhnacademy.front.dashboard.dto.DashboardChartSeriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardChartService {

    private final CoreDashboardChartClient dashboardChartClient;

    public List<DashboardChartResponse> getCharts(Long teamId) {
        return dashboardChartClient.getCharts(teamId);
    }

    public DashboardChartSeriesResponse getChartSeries(Long teamId) {
        return getChartSeries(teamId, null);
    }

    public DashboardChartSeriesResponse getChartSeries(Long teamId, String clientChartId) {
        return dashboardChartClient.getChartSeries(teamId, clientChartId);
    }

    public DashboardChartOptionsResponse getChartOptions(Long teamId) {
        return dashboardChartClient.getChartOptions(teamId);
    }

    public List<DashboardChartResponse> replaceCharts(
            Long teamId,
            DashboardChartReplaceRequest request
    ) {
        return dashboardChartClient.replaceCharts(teamId, request);
    }
}
