package com.nhnacademy.front.rule.dto.flow;

import com.nhnacademy.front.rule.enums.MeasurementType;

import java.util.List;

public record FlowBuildFormResponse (
        Long roomId,
        List<SensorMetaInfo> sensorMetaInfoList
) {
    record SensorMetaInfo(
            MeasurementType measurementType,
            String displayName,
            String description,
            String symbol
    ) {}
}
