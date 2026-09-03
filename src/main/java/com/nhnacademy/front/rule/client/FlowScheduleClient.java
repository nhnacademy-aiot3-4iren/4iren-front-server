package com.nhnacademy.front.rule.client;

import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateRequest;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleCreateResponse;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleListResponse;
import com.nhnacademy.front.rule.dto.flowschedule.FlowScheduleResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "flowScheduleClient",
        path = "/api/rule/rooms"
)
public interface FlowScheduleClient {
    //플로우 스케줄 생성
    @PostMapping("/{room-id}/flows/{flow-id}/schedules")
    ResponseEntity<FlowScheduleCreateResponse> create(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @RequestBody @Valid FlowScheduleCreateRequest request
    );

    //특정 플로우의 스케줄 목록 조회
    @GetMapping("/{room-id}/flows/{flow-id}/schedules")
    ResponseEntity<FlowScheduleListResponse> getList(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId
    );

    @GetMapping("/{room-id}/flows/{flow-id}/schedules/{schedule-id}")
    ResponseEntity<FlowScheduleResponse> getDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @PathVariable("schedule-id") Long scheduleId
    );

    @DeleteMapping("/{room-id}/flows/{flow-id}/schedules/{schedule-id}")
    ResponseEntity<Void> delete(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @PathVariable("schedule-id") Long scheduleId
    );
}
