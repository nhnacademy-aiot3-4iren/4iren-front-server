package com.nhnacademy.front.dashboard.dto;

import java.util.List;

public record DashboardSubscriptionCandidatesResponse(
        List<RoomCandidate> rooms,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public DashboardSubscriptionCandidatesResponse {
        rooms = List.copyOf(rooms);
    }

    public record RoomCandidate(
            Long roomId,
            Long buildingId,
            String buildingName,
            String roomName
    ) {
    }
}
