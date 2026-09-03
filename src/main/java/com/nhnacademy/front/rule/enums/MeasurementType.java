package com.nhnacademy.front.rule.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum MeasurementType {
    CO2("이산화탄소"),
    HUMIDITY("상대습도"),
    ILLUMINATION("주변 조도"),
    TEMPERATURE("온도"),
    PRESSURE("대기압"),
    TVOC("총유기화합물"),
    INFRARED("적외선");

    private final String sensorDesc;

    private static final Map<String, MeasurementType> TYPE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toUpperCase(), Function.identity()));

    MeasurementType(String sensorDesc){
        this.sensorDesc = sensorDesc;
    }



}