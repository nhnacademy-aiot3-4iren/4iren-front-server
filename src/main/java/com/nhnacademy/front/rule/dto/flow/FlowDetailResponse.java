package com.nhnacademy.front.rule.dto.flow;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record FlowDetailResponse (
        Long flowId,

        Long roomId,

        String flowName,

        String description,

        Boolean isActive,

        List<NodeResponse> nodes,

        List<ConnectionResponse> connections,

        List<SensorMetaInfo> sensorMetaInfos,

        String createdAt,

        String updatedAt
){
    record NodeResponse (
            Long nodeId,

            String nodeName,

            String nodeType,

            JsonNode nodeConfig
    ){}
    record ConnectionResponse (
            Long connectionId,
            Long sourceNodeId,
            Long targetNodeId,
            String branchType
    ) {}
    record SensorMetaInfo(
            String measurementType,
            String displayName,
            String description,
            String symbol
    ) {}
}
