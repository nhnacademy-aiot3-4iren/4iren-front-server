package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.dashboard.dto.DashboardResponse;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsRequest;
import com.nhnacademy.front.dashboard.dto.DashboardRoomMetricsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardSubscriptionCandidatesResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetOptionsResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetSeriesResponse;
import com.nhnacademy.front.dashboard.dto.DashboardWidgetUpdateRequest;
import com.nhnacademy.front.dashboard.service.DashboardService;
import com.nhnacademy.front.dashboard.service.DashboardWidgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final DashboardWidgetService dashboardWidgetService;

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

    @GetMapping("/widgets")
    public ResponseEntity<List<DashboardWidgetResponse>> getWidgets(
            @PathVariable @Positive Long teamId
    ) {
        return ResponseEntity.ok(dashboardWidgetService.getWidgets(teamId));
    }

    @GetMapping("/widgets/series")
    public ResponseEntity<DashboardWidgetSeriesResponse> getWidgetSeries(
            @PathVariable @Positive Long teamId,
            @RequestParam(name = "widgetId", required = false) @Size(max = 64) String widgetId
    ) {
        return ResponseEntity.ok(dashboardWidgetService.getWidgetSeries(teamId, widgetId));
    }

    @GetMapping("/widget-options")
    public ResponseEntity<DashboardWidgetOptionsResponse> getWidgetOptions(
            @PathVariable @Positive Long teamId
    ) {
        return ResponseEntity.ok(dashboardWidgetService.getWidgetOptions(teamId));
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

    @PutMapping("/widgets")
    public ResponseEntity<List<DashboardWidgetResponse>> replaceWidgets(
            @PathVariable @Positive Long teamId,
            @Valid @RequestBody DashboardWidgetUpdateRequest request
    ) {
        return ResponseEntity.ok(dashboardWidgetService.replaceWidgets(teamId, request));
    }
}
