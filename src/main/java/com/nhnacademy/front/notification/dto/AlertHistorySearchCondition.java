package com.nhnacademy.front.notification.dto;

import java.time.LocalDate;

public record AlertHistorySearchCondition(
        Long roomId,
        String botType,
        String alertType,
        LocalDate from,
        LocalDate to
) {
}