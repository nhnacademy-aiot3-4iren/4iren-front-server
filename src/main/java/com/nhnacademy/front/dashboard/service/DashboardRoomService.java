package com.nhnacademy.front.dashboard.service;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.dashboard.dto.DashboardRoomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardRoomService {

    private final TeamService teamService;
    private final RoomService roomService;

    public List<DashboardRoomResponse> getSubscribedRooms(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<DashboardRoomResponse> rooms = new ArrayList<>();
        for (TeamResponse team : teamService.getAllTeams()) {
            addTeamSubscriptions(team.teamId(), rooms);
        }
        return rooms.stream()
                .sorted(Comparator.comparing(DashboardRoomResponse::buildingName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(DashboardRoomResponse::roomName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private void addTeamSubscriptions(Long teamId, List<DashboardRoomResponse> rooms) {
        try {
            List<RoomSubscriptionResponse> subscriptions = roomService.getAllSubscriptions(teamId);
            if (subscriptions == null) {
                return;
            }
            for (RoomSubscriptionResponse subscription : subscriptions) {
                addRoom(teamId, subscription, rooms);
            }
        } catch (RuntimeException exception) {
            log.warn("Dashboard room subscriptions could not be loaded. teamId={}", teamId, exception);
        }
    }

    private void addRoom(
            Long teamId,
            RoomSubscriptionResponse subscription,
            List<DashboardRoomResponse> rooms
    ) {
        try {
            RoomDetailResponse detail = roomService.getRoom(teamId, subscription.roomId());
            rooms.add(new DashboardRoomResponse(
                    teamId,
                    detail.roomId(),
                    detail.buildingId(),
                    detail.buildingName(),
                    detail.roomName()
            ));
        } catch (RuntimeException exception) {
            log.warn("Dashboard room detail could not be loaded. teamId={}, roomId={}",
                    teamId, subscription.roomId(), exception);
        }
    }
}
