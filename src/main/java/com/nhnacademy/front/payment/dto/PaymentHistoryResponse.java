package com.nhnacademy.front.payment.dto;

import java.time.LocalDateTime;

public record PaymentHistoryResponse(
        Long id,
        Long amount,
        String status,
        String failureReason,
        LocalDateTime attemptedAt,
        LocalDateTime approvedAt
) {
}
