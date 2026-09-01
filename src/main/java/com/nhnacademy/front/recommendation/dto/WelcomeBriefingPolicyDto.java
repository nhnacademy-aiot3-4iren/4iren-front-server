package com.nhnacademy.front.recommendation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WelcomeBriefingPolicyDto(
        @NotNull @Min(0) @Max(100) Integer rainPossibleProbability,
        @NotNull @Min(0) @Max(100) Integer rainExpectedProbability,
        @NotNull @DecimalMin("0.0") Double strongWindSpeed,
        @NotNull @Min(0) @Max(100) Integer highHumidityPercent,
        @NotNull Boolean enabled
) {
    @AssertTrue(message = "rainPossibleProbability must be less than or equal to rainExpectedProbability")
    public boolean isRainProbabilityOrderValid() {
        return rainPossibleProbability == null
                || rainExpectedProbability == null
                || rainPossibleProbability <= rainExpectedProbability;
    }
}
