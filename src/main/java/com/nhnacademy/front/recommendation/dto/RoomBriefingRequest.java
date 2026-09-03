package com.nhnacademy.front.recommendation.dto;

import java.time.LocalDate;

public record RoomBriefingRequest(
        Long teamId,
        Long roomId,
        LocalDate date,
        Integer startHour,
        Integer endHour
) {
    public static RoomBriefingRequest welcome(Long teamId, Long roomId) {
        return new RoomBriefingRequest(teamId, roomId, null, null, null);
    }

    public static RoomBriefingRequest dailySummary(Long teamId, Long roomId) {
        return new RoomBriefingRequest(teamId, roomId, null, null, null);
    }
}
