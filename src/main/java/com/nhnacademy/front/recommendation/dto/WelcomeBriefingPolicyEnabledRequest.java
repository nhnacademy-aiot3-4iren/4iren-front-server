package com.nhnacademy.front.recommendation.dto;

import jakarta.validation.constraints.NotNull;

public record WelcomeBriefingPolicyEnabledRequest(
        @NotNull Boolean enabled
) {
}
