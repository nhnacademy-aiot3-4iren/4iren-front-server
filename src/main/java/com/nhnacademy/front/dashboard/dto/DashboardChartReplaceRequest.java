package com.nhnacademy.front.dashboard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DashboardChartReplaceRequest(
        @NotNull(message = "차트 목록은 null일 수 없습니다.")
        @Size(max = 4, message = "차트는 최대 4개까지 저장할 수 있습니다.")
        List<@Valid Chart> charts
) {
    public record Chart(
            @NotBlank
            @Size(max = 64)
            String clientChartId,

            @NotNull
            @Positive
            Long roomId,

            @NotBlank
            @Size(max = 50)
            String metricCode,

            @NotBlank
            @Size(max = 50)
            String displayName,

            @NotNull
            @Size(max = 32)
            String symbol,

            @NotBlank
            @Pattern(regexp = "1H|6H|24H|7D|30D")
            String timeRange
    ) {
    }
}
