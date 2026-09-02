package com.nhnacademy.front.subscription.service;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.subscription.dto.SubscribedRoomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribedRoomService {

    private final TeamService teamService;
    private final RoomService roomService;

    public List<SubscribedRoomResponse> getSubscribedRooms() {
        List<SubscribedRoomResponse> rooms = new ArrayList<>();

        for (TeamResponse team : teamService.getAllTeams()) {
            addTeamSubscriptions(team.teamId(), rooms);
        }

        return rooms.stream()
                .sorted(Comparator.comparing(SubscribedRoomResponse::buildingName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(SubscribedRoomResponse::roomName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private void addTeamSubscriptions(Long teamId, List<SubscribedRoomResponse> rooms) {
        try {
            List<RoomSubscriptionResponse> subscriptions = roomService.getAllSubscriptions(teamId);
            if (subscriptions == null) {
                return;
            }

            for (RoomSubscriptionResponse subscription : subscriptions) {
                addRoom(teamId, subscription.roomId(), rooms);
            }
        } catch (RuntimeException exception) {
            log.warn("Subscribed rooms could not be loaded. teamId={}", teamId, exception);
        }
    }

    private void addRoom(Long teamId, Long roomId, List<SubscribedRoomResponse> rooms) {
        try {
            RoomDetailResponse room = roomService.getRoom(teamId, roomId);
            rooms.add(new SubscribedRoomResponse(
                    teamId,
                    room.buildingId(),
                    room.roomId(),
                    room.buildingName(),
                    room.roomName()
            ));
        } catch (RuntimeException exception) {
            log.warn("Subscribed room detail could not be loaded. teamId={}, roomId={}",
                    teamId, roomId, exception);
        }
    }
}
