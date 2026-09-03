package com.nhnacademy.front.rule.controller;

import com.nhnacademy.front.core.dto.building.BuildingResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.rule.dto.flow.FlowListResponse;
import com.nhnacademy.front.rule.service.FlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * 플로우 화면(목록 / 편집기)을 반환한다.
 * 사이드바 트리는 모든 화면에서 공통으로 필요하므로 여기서 함께 채운다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class FlowPageController {

    private static final String VIEW_LIST = "flow/flow-list";
    private static final String VIEW_BUILDER = "flow/flow-diy";

    private final FlowService flowService;

    /**
     * 강의실을 아직 고르지 않은 상태.
     * 사이드바만 채우고 본문에는 안내 화면을 띄운다.
     */
    @GetMapping("/flow")
    public String flowHome(Model model) {
        addSidebar(model, null);
        model.addAttribute("room", null);
        model.addAttribute("flows", Collections.emptyList());
        return VIEW_LIST;
    }

    /**
     * 특정 강의실의 플로우 목록
     */
    @GetMapping("/rooms/{roomId}/flows")
    public String flowList(@PathVariable("roomId") Long roomId, Model model) {
        RoomDetailResponse room = flowService.getRoom(roomId);
        addSidebar(model, room);

        FlowListResponse response = flowService.getFlows(roomId);
        model.addAttribute("room", room);
        model.addAttribute("flows",
                response == null || response.flowResponseList() == null
                        ? Collections.emptyList()
                        : response.flowResponseList());

        return VIEW_LIST;
    }

    /**
     * 새 플로우 편집기.
     * templateId가 있으면 화면에서 템플릿 상세를 불러와 캔버스를 채운다.
     */
    @GetMapping("/rooms/{roomId}/flows/new")
    public String newFlow(@PathVariable("roomId") Long roomId,
                          @RequestParam(value = "templateId", required = false) Long templateId,
                          Model model) {
        model.addAttribute("room", flowService.getRoom(roomId));
        model.addAttribute("flow", null);
        model.addAttribute("templateId", templateId);
        return VIEW_BUILDER;
    }

    /**
     * 기존 플로우 편집기.
     * 노드와 커넥션은 화면에서 상세 API로 불러온다.
     */
    @GetMapping("/rooms/{roomId}/flows/{flowId}")
    public String editFlow(@PathVariable("roomId") Long roomId,
                           @PathVariable("flowId") Long flowId,
                           Model model) {
        model.addAttribute("room", flowService.getRoom(roomId));
        model.addAttribute("flowId", flowId);
        model.addAttribute("templateId", null);
        return VIEW_BUILDER;
    }

    /* ---------------- 내부 ---------------- */

    /**
     * 사이드바 트리에 필요한 값을 모델에 담는다.
     * 현재 강의실이 속한 건물만 펼친 상태로 렌더링하고, 나머지는 화면에서 지연 로딩한다.
     */
    private void addSidebar(Model model, RoomDetailResponse room) {
        List<BuildingResponse> buildings = flowService.getBuildings();
        model.addAttribute("buildings", buildings);

        Long openBuildingId = room == null ? null : room.buildingId();
        model.addAttribute("openBuildingId", openBuildingId);
        model.addAttribute("openRooms",
                openBuildingId == null
                        ? Collections.<RoomResponse>emptyList()
                        : flowService.getRooms(openBuildingId));
    }
}
