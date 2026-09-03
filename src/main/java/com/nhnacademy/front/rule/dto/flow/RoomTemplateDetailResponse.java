package com.nhnacademy.front.rule.dto.flow;

import com.nhnacademy.front.rule.dto.jsoninfo.NodeConfig;
import com.nhnacademy.front.rule.enums.MeasurementType;
import com.nhnacademy.front.rule.enums.NodeType;

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

            NodeType nodeType,

            NodeConfig nodeConfig
    ){}
    record ConnectionResponse (
            Long connectionId,
            Long sourceNodeId,
            Long targetNodeId,
            String branchType
    ) {}
    record SensorMetaInfo(
            MeasurementType measurementType,
            String displayName,
            String description,
            String symbol
    ) {}

}