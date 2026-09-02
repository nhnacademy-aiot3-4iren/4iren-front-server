package com.nhnacademy.front.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionStatus;
import com.nhnacademy.front.core.dto.subscription.TeamRoomNotifications;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.BuildingService;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private static final int FETCH_ALL_SIZE = 100;
    private static final String DEFAULT_SORT = "id,ASC";

    private final TeamService teamService;
    private final BuildingService buildingService;
    private final RoomService roomService;

    @GetMapping("/")
    public String getHome() {
        return "start";
    }

    @GetMapping("/forgot")
    public String getForgot() {
        return "account/forgot";
    }

    @GetMapping("/flowdiy")
    public String getFlowdiy() {
        return "flow-diy";
    }

    @GetMapping("/table")
    public String getAlertHis() {
        // basic/table -> layout/table 로 변경
        return "layout/table";
    }

    @GetMapping("/team")
    public String getMyTeams() {
        // sidebar-menu/team/team-list -> team/team-list 로 뎁스 축소
        return "team/team-list";
    }

    @GetMapping("/alert-history")
    public String getAlertHistory() { return "alarms/alert-history"; }

    @GetMapping("/settings")
    public String getSettings(@ModelAttribute("role") String role, Model model) {
        if ( role != null) {
            model.addAttribute("role", role);
            model.addAttribute("isAdmin", role.equals("ADMIN") || role.equals("OWNER"));
        }
        return "settings/settings";
    }

    @GetMapping("/settings/notifications/rooms")
    public String getNotificationRooms(Model model) {
        List<TeamDetailResponse> teams = teamService.getTeams(0, FETCH_ALL_SIZE, DEFAULT_SORT).content();
        model.addAttribute("teamGroups", teams.stream().map(this::buildTeamGroup).toList());
        return "settings/notification-rooms";
    }

    private TeamRoomNotifications buildTeamGroup(TeamDetailResponse team) {
        PageResponse<BuildingDetailResponse> buildings =
                buildingService.getBuildings(team.teamId(), 0, FETCH_ALL_SIZE, DEFAULT_SORT);

        List<TeamRoomNotifications.BuildingGroup> buildingGroups = buildings.content().stream()
                .map(building -> buildBuildingGroup(team.teamId(), building))
                .toList();

        return new TeamRoomNotifications(team, buildingGroups);
    }

    private TeamRoomNotifications.BuildingGroup buildBuildingGroup(Long teamId, BuildingDetailResponse building) {
        List<RoomDetailResponse> rooms =
                roomService.getRooms(teamId, building.buildingId(), 0, FETCH_ALL_SIZE, DEFAULT_SORT).content();

        Map<Long, RoomSubscriptionStatus> statuses = roomService.getSubscriptionStatuses(
                teamId,
                rooms.stream().map(RoomDetailResponse::roomId).toList()
        );

        List<TeamRoomNotifications.RoomRow> rows = rooms.stream()
                .map(room -> new TeamRoomNotifications.RoomRow(room, statuses.get(room.roomId())))
                .toList();

        return new TeamRoomNotifications.BuildingGroup(building.buildingId(), building.buildingName(), rows);
    }
}
