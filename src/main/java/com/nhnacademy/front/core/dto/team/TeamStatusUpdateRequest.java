package com.nhnacademy.front.core.dto.team;

import jakarta.validation.constraints.NotNull;

public record TeamStatusUpdateRequest(
        @NotNull(message = "팀 상태는 null일 수 없습니다.")
        TeamStatus status
) {
}
