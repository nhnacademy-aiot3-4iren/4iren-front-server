package com.nhnacademy.front.payment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Plan plan,
        Long amount,
        SubscriptionStatus status,
        LocalDateTime currentPeriodEnd,
        LocalDate nextBillingDate
) {
}
