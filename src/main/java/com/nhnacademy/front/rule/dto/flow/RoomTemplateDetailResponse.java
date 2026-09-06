package com.nhnacademy.front.rule.dto.flow;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record RoomTemplateDetailResponse(

        String templateName,

        String description,

        List<NodeResponse> nodes,

        List<ConnectionResponse> connections,

        List<SensorMetaInfo> sensorMetaInfos
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
