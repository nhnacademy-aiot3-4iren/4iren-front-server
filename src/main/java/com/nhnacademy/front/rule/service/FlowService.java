package com.nhnacademy.front.rule.service;

import com.nhnacademy.front.core.client.CoreBuildingClient;
import com.nhnacademy.front.core.client.CoreRoomClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.rule.client.FlowClient;
import com.nhnacademy.front.rule.client.FlowScheduleClient;
import com.nhnacademy.front.rule.client.NodeConfigClient;
import com.nhnacademy.front.rule.dto.flow.*;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateRequest;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateResponse;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleListResponse;
import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidateRequest;
import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 플로우 화면이 필요로 하는 조회/변경을 한 곳에 모은다.
 * 강의실·건물은 core 쪽 클라이언트를, 플로우는 rule 쪽 클라이언트를 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowService {

    /** 사이드바 트리는 한 번에 다 보여주므로 넉넉한 크기로 조회한다. */
    private static final int TREE_PAGE_SIZE = 100;
    private static final String TREE_SORT = "id,ASC";

    private final FlowClient flowClient;
    private final FlowScheduleClient flowScheduleClient;
    private final NodeConfigClient nodeConfigClient;

    private final CoreBuildingClient coreBuildingClient;
    private final CoreRoomClient coreRoomClient;
    private final TeamService teamService;

    /* ================================================================
       팀 / 건물 / 강의실
       ================================================================ */

    /**
     * 현재 사용자의 기본 팀 id.
     * GlobalControllerAdvice 와 같은 기준(내 팀 목록의 첫 번째)을 사용한다.
     */
    public Long resolveTeamId() {
        PageResponse<TeamDetailResponse> teams = teamService.getTeams(0, 1, TREE_SORT);
        if (teams == null || teams.content() == null || teams.content().isEmpty()) {
            throw new IllegalStateException("소속된 팀을 찾을 수 없습니다.");
        }
        return teams.content().get(0).teamId();
    }

    /** 사이드바 트리에 쓸 건물 목록 */
    public List<BuildingResponse> getBuildings() {
        PageResponse<BuildingResponse> page =
                coreBuildingClient.getBuildings(resolveTeamId(), 0, TREE_PAGE_SIZE, TREE_SORT);
        return page == null || page.content() == null ? Collections.emptyList() : page.content();
    }

    /** 특정 건물의 강의실 목록 */
    public List<RoomResponse> getRooms(Long buildingId) {
        PageResponse<RoomResponse> page =
                coreRoomClient.getRooms(resolveTeamId(), buildingId, 0, TREE_PAGE_SIZE, TREE_SORT);
        return page == null || page.content() == null ? Collections.emptyList() : page.content();
    }

    /** 현재 강의실 정보 (브레드크럼, 제목에 사용) */
    public RoomDetailResponse getRoom(Long roomId) {
        return coreRoomClient.getRoom(resolveTeamId(), roomId);
    }

    /* ================================================================
       플로우
       ================================================================ */

    public FlowListResponse getFlows(Long roomId) {
        return flowClient.getFlowList(roomId).getBody();
    }

    public FlowDetailResponse getFlowDetail(Long roomId, Long flowId) {
        return flowClient.getFlowDetail(roomId, flowId).getBody();
    }

    public FlowBuildFormResponse getBuildForm(Long roomId) {
        return flowClient.buildForm(roomId).getBody();
    }

    public FlowCreateResponse createFlow(Long roomId, FlowCreateRequest request) {
        return flowClient.createFlow(roomId, request).getBody();
    }

    public void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request) {
        flowClient.updateFlow(roomId, flowId, request);
    }

    public void deleteFlow(Long roomId, Long flowId) {
        flowClient.deleteFlow(roomId, flowId);
    }

    public void updateStatus(Long roomId, Long flowId, UpdateFlowStatusRequest request) {
        flowClient.updateStatus(roomId, flowId, request);
    }

    /* ================================================================
       템플릿
       ================================================================ */

    public RoomTemplateListResponse getTemplates(Long roomId) {
        return flowClient.getFlowTemplateList(roomId).getBody();
    }

    public RoomTemplateDetailResponse getTemplateDetail(Long roomId, Long templateId) {
        return flowClient.getTemplateFlowDetail(roomId, templateId).getBody();
    }

    /* ================================================================
       스케줄
       ================================================================ */

    public FlowScheduleListResponse getSchedules(Long roomId, Long flowId) {
        return flowScheduleClient.getList(roomId, flowId).getBody();
    }

    public FlowScheduleCreateResponse createSchedule(Long roomId, Long flowId,
                                                     FlowScheduleCreateRequest request) {
        return flowScheduleClient.create(roomId, flowId, request).getBody();
    }

    public void deleteSchedule(Long roomId, Long flowId, Long scheduleId) {
        flowScheduleClient.delete(roomId, flowId, scheduleId);
    }

    /* ================================================================
       노드 설정 검증
       ================================================================ */

    public NodeConfigValidationResponse validateNodeConfig(Long roomId,
                                                           NodeConfigValidateRequest request) {
        return nodeConfigClient.validateNodeConfig(roomId, request).getBody();
    }
}