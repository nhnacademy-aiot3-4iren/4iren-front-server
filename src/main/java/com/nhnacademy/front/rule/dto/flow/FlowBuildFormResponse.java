package com.nhnacademy.front.rule.dto.flow;

import java.util.List;

public record FlowBuildFormResponse (
        Long roomId,
        List<SensorMetaInfo> sensorMetaInfoList
) {
    record SensorMetaInfo(
            String measurementType,
            String displayName,
            String description,
            String symbol
    ) {}
}
