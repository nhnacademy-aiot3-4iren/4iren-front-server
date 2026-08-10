package com.nhnacademy.front.notification.dto;

import java.time.LocalDateTime;

public record AlertHistoryResponse(
        Long alertHistoryId,
        Long roomId,
        String botType,
        String alertType,
        String message,
        LocalDateTime sendAt
) {
}