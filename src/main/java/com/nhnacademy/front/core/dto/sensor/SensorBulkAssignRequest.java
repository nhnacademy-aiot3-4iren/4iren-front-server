package com.nhnacademy.front.core.dto.sensor;

import java.util.List;

public record SensorBulkAssignRequest(
        List<String> sensorIds
) {}
