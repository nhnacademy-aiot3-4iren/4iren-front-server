package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.dashboard.dto.*;
import com.nhnacademy.front.dashboard.service.DashboardChartService;
import com.nhnacademy.front.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final DashboardChartService dashboardChartService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable @Positive Long teamId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(name = "query", defaultValue = "") @Size(max = 100) String query,
            @RequestParam(name = "metricCode", required = false)
            @Size(max = 4) List<@NotBlank @Size(max = 50) String> metricCodes
    ) {
        return ResponseEntity.ok(dashboardService.getDashboard(
                teamId,
                page,
                size,
                query,
                metricCodes
        ));
    }

    @GetMapping("/charts")
    public ResponseEntity<List<DashboardChartResponse>> getCharts(
            @PathVariable @Positive Long teamId
    ) {
        return ResponseEntity.ok(dashboardChartService.getCharts(teamId));
    }

    @GetMapping("/charts/series")
    public ResponseEntity<DashboardChartSeriesResponse> getChartSeries(
            @PathVariable @Positive Long teamId,
            @RequestParam(name = "clientChartId", required = false)
            @Size(max = 64) String clientChartId
    ) {
        return ResponseEntity.ok(dashboardChartService.getChartSeries(teamId, clientChartId));
    }

    @GetMapping("/charts/options")
    public ResponseEntity<DashboardChartOptionsResponse> getChartOptions(
            @PathVariable @Positive Long teamId
    ) {
        return ResponseEntity.ok(dashboardChartService.getChartOptions(teamId));
    }

    @PostMapping("/room-metrics")
    public ResponseEntity<DashboardRoomMetricsResponse> getRoomMetrics(
            @PathVariable @Positive Long teamId,
            @Valid @RequestBody DashboardRoomMetricsRequest request
    ) {
        return ResponseEntity.ok(dashboardService.getRoomMetrics(teamId, request));
    }

    @GetMapping("/subscription-candidates")
    public ResponseEntity<DashboardSubscriptionCandidatesResponse> getSubscriptionCandidates(
            @PathVariable @Positive Long teamId,
            @RequestParam(name = "query", defaultValue = "") @Size(max = 50) String query,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return ResponseEntity.ok(dashboardService.getSubscriptionCandidates(
                teamId,
                query,
                page,
                size
        ));
    }

    @PutMapping("/charts")
    public ResponseEntity<List<DashboardChartResponse>> replaceCharts(
            @PathVariable @Positive Long teamId,
            @Valid @RequestBody DashboardChartReplaceRequest request
    ) {
        return ResponseEntity.ok(dashboardChartService.replaceCharts(teamId, request));
    }
}
