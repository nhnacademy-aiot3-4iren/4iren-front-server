package com.nhnacademy.front.rule.controller;

import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.rule.dto.ApiErrorResponse;
import com.nhnacademy.front.rule.dto.flow.*;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateRequest;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateResponse;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleListResponse;
import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidateRequest;
import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidationResponse;
import com.nhnacademy.front.rule.service.FlowService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 화면 JS가 fetch로 호출하는 프록시.
 * 룰 엔진 응답을 그대로 전달하고, 실패하면 message만 담아 내려준다.
 */
@Slf4j
@RestController
@RequestMapping("/flows/api")
@RequiredArgsConstructor
public class FlowApiController {

    private final FlowService flowService;

    /* ---------------- 사이드바 ---------------- */

    /** 건물을 펼칠 때 그 건물의 강의실을 지연 로딩한다. */
    @GetMapping("/buildings/{buildingId}/rooms")
    public Map<String, List<RoomResponse>> getRooms(@PathVariable("buildingId") Long buildingId) {
        return Map.of("content", flowService.getRooms(buildingId));
    }

    /* ---------------- 플로우 ---------------- */

    @GetMapping("/rooms/{roomId}/flows/form")
    public FlowBuildFormResponse getForm(@PathVariable("roomId") Long roomId) {
        return flowService.getBuildForm(roomId);
    }

    @GetMapping("/rooms/{roomId}/flows/{flowId}")
    public FlowDetailResponse getFlowDetail(@PathVariable("roomId") Long roomId,
                                            @PathVariable("flowId") Long flowId) {
        return flowService.getFlowDetail(roomId, flowId);
    }

    @PostMapping("/rooms/{roomId}/flows")
    public ResponseEntity<FlowCreateResponse> createFlow(@PathVariable("roomId") Long roomId,
                                                         @RequestBody FlowCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flowService.createFlow(roomId, request));
    }

    @PutMapping("/rooms/{roomId}/flows/{flowId}")
    public ResponseEntity<Void> updateFlow(@PathVariable("roomId") Long roomId,
                                           @PathVariable("flowId") Long flowId,
                                           @RequestBody FlowUpdateRequest request) {
        flowService.updateFlow(roomId, flowId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rooms/{roomId}/flows/{flowId}")
    public ResponseEntity<Void> deleteFlow(@PathVariable("roomId") Long roomId,
                                           @PathVariable("flowId") Long flowId) {
        flowService.deleteFlow(roomId, flowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/rooms/{roomId}/flows/{flowId}/active")
    public ResponseEntity<Void> updateActive(@PathVariable("roomId") Long roomId,
                                             @PathVariable("flowId") Long flowId,
                                             @RequestBody UpdateFlowStatusRequest request) {
        flowService.updateStatus(roomId, flowId, request);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- 템플릿 ---------------- */

    @GetMapping("/rooms/{roomId}/flow-templates")
    public RoomTemplateListResponse getTemplates(@PathVariable("roomId") Long roomId) {
        return flowService.getTemplates(roomId);
    }

    @GetMapping("/rooms/{roomId}/flow-templates/{templateId}")
    public RoomTemplateDetailResponse getTemplateDetail(@PathVariable("roomId") Long roomId,
                                                        @PathVariable("templateId") Long templateId) {
        return flowService.getTemplateDetail(roomId, templateId);
    }

    /* ---------------- 스케줄 ---------------- */

    @GetMapping("/rooms/{roomId}/flows/{flowId}/schedules")
    public FlowScheduleListResponse getSchedules(@PathVariable("roomId") Long roomId,
                                                 @PathVariable("flowId") Long flowId) {
        return flowService.getSchedules(roomId, flowId);
    }

    @PostMapping("/rooms/{roomId}/flows/{flowId}/schedules")
    public ResponseEntity<FlowScheduleCreateResponse> createSchedule(
            @PathVariable("roomId") Long roomId,
            @PathVariable("flowId") Long flowId,
            @RequestBody FlowScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flowService.createSchedule(roomId, flowId, request));
    }

    @DeleteMapping("/rooms/{roomId}/flows/{flowId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable("roomId") Long roomId,
                                               @PathVariable("flowId") Long flowId,
                                               @PathVariable("scheduleId") Long scheduleId) {
        flowService.deleteSchedule(roomId, flowId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- 노드 설정 검증 ---------------- */

    @PostMapping("/rooms/{roomId}/node-config/{nodeId}")
    public NodeConfigValidationResponse validateNodeConfig(
            @PathVariable("roomId") Long roomId,
            @PathVariable("nodeId") Long nodeId,
            @RequestBody NodeConfigValidateRequest request) {
        return flowService.validateNodeConfig(roomId, request);
    }

    /* ---------------- 예외 처리 ---------------- */

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeign(FeignException e) {
        log.warn("룰 엔진 호출 실패. status={}, message={}", e.status(), e.getMessage());

        HttpStatus status = e.status() > 0 ? HttpStatus.resolve(e.status()) : null;
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse("요청을 처리하지 못했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception e) {
        log.error("플로우 API 처리 중 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("요청을 처리하지 못했습니다."));
    }
}