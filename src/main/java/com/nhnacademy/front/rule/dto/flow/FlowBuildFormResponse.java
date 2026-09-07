package com.nhnacademy.front.rule.dto.flow;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record FlowBuildFormResponse (
        Long roomId,

        @JsonAlias({"sensorMetaInfos", "sensorMetaInfo"})
        List<SensorMetaInfo> sensorMetaInfoList
) {
    record SensorMetaInfo(
            @JsonAlias({"metricCode", "MeasurementType"})
            String measurementType,
            String displayName,
            String description,
            String symbol
    ) {}
}
