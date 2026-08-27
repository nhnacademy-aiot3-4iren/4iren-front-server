package com.nhnacademy.front.recommendation.dto;

import java.util.List;

public record DailySummaryResponse(
        String summary,
        String indoorEnvironment,
        String outdoorEnvironment,
        String comparison,
        List<String> recommendations,
        List<String> checks
) {
}
