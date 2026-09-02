package com.nhnacademy.front.rule.client;

import com.nhnacademy.front.rule.dto.flow.*;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "flowClient",
        path = "/api/rule/rooms/{room-id}"
)
public interface FlowClient {

    //플로우 빌드 폼 호출
    @GetMapping("/flows/form")
    ResponseEntity<FlowBuildFormResponse> buildForm(
            @PathVariable("room-id") Long roomId
    );

    //플로우 생성
    @PostMapping("/flows")
    ResponseEntity<FlowCreateResponse> createFlow(
            @PathVariable("room-id") Long roomId,
            @Valid @RequestBody FlowCreateRequest request
    );

    //플로우 목록 조회
    @GetMapping("/flows")
    ResponseEntity<FlowListResponse> getFlowList(
            @PathVariable("room-id") Long roomId
    );

    //강의실 별 템플릿 플로우 제안 목록
    @GetMapping("/flow-templates")
    ResponseEntity<RoomTemplateListResponse> getFlowTemplateList(
            @PathVariable("room-id") Long roomId
    );

    //플로우 단건(상세) 조회 및 수정 폼
    @GetMapping("/flows/{flow-id}")
    ResponseEntity<FlowDetailResponse> getFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId
    );

    //추천 템플릿 플로우 상세 조회 및 플로우 생성 폼 화면(강의실별 플로우 관리자 전용)
    @GetMapping("/flow-templates/{template-id}")
    ResponseEntity<RoomTemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("template-id") Long templateId
    );

    //플로우 수정
    @PutMapping("/flows/{flow-id}")
    ResponseEntity<Void> updateFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request
    );

    //플로우 삭제
    @DeleteMapping("/flows/{flow-id}")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId
    );

    //플로우 활성화/비활성화 설정
    @PatchMapping("/flows/{flow-id}/active")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @RequestBody  @Valid UpdateFlowStatusRequest request
    );
}

