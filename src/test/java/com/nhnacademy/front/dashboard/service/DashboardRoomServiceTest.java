package com.nhnacademy.front.dashboard.service;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.dto.team.TeamRole;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DashboardRoomServiceTest {

    private final TeamService teamService = mock(TeamService.class);
    private final RoomService roomService = mock(RoomService.class);
    private final DashboardRoomService service =
            new DashboardRoomService(teamService, roomService);

    @Test
    void mapsCurrentUsersSubscriptionsToRoomsWithBuildingInformation() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L)));
        when(roomService.getAllSubscriptions(1L)).thenReturn(List.of(subscription(10L)));
        when(roomService.getRoom(1L, 10L))
                .thenReturn(new RoomDetailResponse(10L, 20L, "본관", "회의실", null, 3, 1));

        assertThat(service.getSubscribedRooms(7L)).singleElement().satisfies(room -> {
            assertThat(room.teamId()).isEqualTo(1L);
            assertThat(room.roomId()).isEqualTo(10L);
            assertThat(room.buildingName()).isEqualTo("본관");
        });
        verify(roomService).getAllSubscriptions(1L);
    }

    @Test
    void oneBrokenRoomDoesNotBreakTheRoomList() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L)));
        when(roomService.getAllSubscriptions(1L))
                .thenReturn(List.of(subscription(10L), subscription(11L)));
        when(roomService.getRoom(1L, 10L)).thenThrow(new RuntimeException("core error"));
        when(roomService.getRoom(1L, 11L))
                .thenReturn(new RoomDetailResponse(11L, 21L, "별관", "정상 공간", null, 0, 0));

        assertThat(service.getSubscribedRooms(7L))
                .extracting("roomId")
                .containsExactly(11L);
    }

    @Test
    void mergesPublicSubscriptionsFromMultipleTeams() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L), team(2L)));
        when(roomService.getAllSubscriptions(1L)).thenReturn(List.of(subscription(10L)));
        when(roomService.getAllSubscriptions(2L)).thenReturn(List.of(subscription(20L)));
        when(roomService.getRoom(1L, 10L))
                .thenReturn(new RoomDetailResponse(10L, 100L, "본관", "회의실", null, 0, 0));
        when(roomService.getRoom(2L, 20L))
                .thenReturn(new RoomDetailResponse(20L, 200L, "별관", "강의실", null, 0, 0));

        assertThat(service.getSubscribedRooms(7L))
                .extracting("teamId", "roomId")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, 10L),
                        org.assertj.core.groups.Tuple.tuple(2L, 20L)
                );
        verify(roomService).getAllSubscriptions(1L);
        verify(roomService).getAllSubscriptions(2L);
    }

    @Test
    void teamWithoutSubscriptionsAddsNoRooms() {
        when(teamService.getAllTeams()).thenReturn(List.of(team(1L), team(2L)));
        when(roomService.getAllSubscriptions(1L)).thenReturn(List.of());
        when(roomService.getAllSubscriptions(2L)).thenReturn(List.of(subscription(20L)));
        when(roomService.getRoom(2L, 20L))
                .thenReturn(new RoomDetailResponse(20L, 200L, "별관", "강의실", null, 0, 0));

        assertThat(service.getSubscribedRooms(7L))
                .extracting("roomId")
                .containsExactly(20L);
    }

    @Test
    void userWithoutAuthenticationHasNoDashboardRooms() {
        assertThat(service.getSubscribedRooms(null)).isEmpty();
        verifyNoInteractions(teamService, roomService);
    }

    private TeamResponse team(Long teamId) {
        return new TeamResponse(teamId, "팀", null, TeamRole.NORMAL);
    }

    private RoomSubscriptionResponse subscription(Long roomId) {
        return new RoomSubscriptionResponse(roomId + 1000, roomId, true);
    }
}
