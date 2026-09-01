package com.nhnacademy.front.core.dto.subscription;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;

import java.util.List;

public record TeamRoomNotifications(
        TeamDetailResponse team,
        List<BuildingGroup> buildings
) {
    public record BuildingGroup(
            Long buildingId,
            String buildingName,
            List<RoomRow> rooms
    ) {
    }

    public record RoomRow(
            RoomDetailResponse room,
            RoomSubscriptionStatus status
    ) {
    }
}
