package com.nhnacademy.front.rule.dto.jsoninfo.condition;


import com.nhnacademy.front.rule.dto.jsoninfo.NodeConfig;
import com.nhnacademy.front.rule.enums.MeasurementType;
import com.nhnacademy.front.rule.enums.NodeType;
import com.nhnacademy.front.rule.enums.Operator;
import jakarta.validation.constraints.NotNull;

public record ThresholdNodeConfig (

        @NotNull
        NodeType nodeType,

        @NotNull
        Integer x,

        @NotNull
        Integer y,

        @NotNull
        MeasurementType measurementType,

        @NotNull
        String unit,

        @NotNull
        Operator operator,

        @NotNull
        Double threshold

)implements NodeConfig {
        @Override
        public MeasurementType measurementType(){
                return measurementType;
        }
}
