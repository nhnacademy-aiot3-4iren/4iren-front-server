package com.nhnacademy.front.subscription.service;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.subscription.dto.SubscribedRoomResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SubscribedRoomServiceTest {

    private final TeamService teamService = mock(TeamService.class);
    private final RoomService roomService = mock(RoomService.class);
    private final SubscribedRoomService service = new SubscribedRoomService(teamService, roomService);

    @Test
    void collectsSubscriptionsFromEveryTeam() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L), team(2L)));
        when(roomService.getAllSubscriptions(1L)).thenReturn(List.of(subscription(10L)));
        when(roomService.getAllSubscriptions(2L)).thenReturn(List.of(subscription(20L)));
        when(roomService.getRoom(1L, 10L)).thenReturn(room(10L, 100L, "B Building", "Room B"));
        when(roomService.getRoom(2L, 20L)).thenReturn(room(20L, 200L, "A Building", "Room A"));

        List<SubscribedRoomResponse> result = service.getSubscribedRooms();

        assertThat(result).extracting(SubscribedRoomResponse::teamId).containsExactly(2L, 1L);
        assertThat(result).extracting(SubscribedRoomResponse::roomId).containsExactly(20L, 10L);
        verify(roomService).getAllSubscriptions(1L);
        verify(roomService).getAllSubscriptions(2L);
    }

    @Test
    void teamWithoutSubscriptionsDoesNotFailWholeList() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L), team(2L)));
        when(roomService.getAllSubscriptions(1L)).thenThrow(new RuntimeException("unavailable"));
        when(roomService.getAllSubscriptions(2L)).thenReturn(List.of(subscription(20L)));
        when(roomService.getRoom(2L, 20L)).thenReturn(room(20L, 200L, "Building", "Room"));

        assertThat(service.getSubscribedRooms()).hasSize(1);
    }

    @Test
    void returnsEmptyListWhenNothingIsSubscribed() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L)));
        when(roomService.getAllSubscriptions(1L)).thenReturn(List.of());

        assertThat(service.getSubscribedRooms()).isEmpty();
        verify(roomService, never()).getRoom(anyLong(), anyLong());
    }

    private TeamResponse team(Long teamId) {
        return new TeamResponse(teamId, "Team " + teamId, null, null);
    }

    private RoomSubscriptionResponse subscription(Long roomId) {
        return new RoomSubscriptionResponse(1L, roomId, true);
    }

    private RoomDetailResponse room(Long roomId, Long buildingId, String buildingName, String roomName) {
        return new RoomDetailResponse(roomId, buildingId, buildingName, roomName, null, 0L, 0L);
    }
}
