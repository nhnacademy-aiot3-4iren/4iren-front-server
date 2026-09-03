package com.nhnacademy.front.rule.dto.jsoninfo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.front.rule.dto.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.logical.OrNodeConfig;
import com.nhnacademy.front.rule.dto.jsoninfo.start.StartNodeConfig;
import com.nhnacademy.front.rule.enums.MeasurementType;
import com.nhnacademy.front.rule.enums.NodeType;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nodeType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThresholdNodeConfig.class, name = "THRESHOLD"),
        @JsonSubTypes.Type(value = GradientNodeConfig.class, name = "GRADIENT"),
        @JsonSubTypes.Type(value = AverageNodeConfig.class, name = "AVERAGE"),
        @JsonSubTypes.Type(value = DurationNodeConfig.class, name = "DURATION"),

        @JsonSubTypes.Type(value = OrNodeConfig.class, name = "OR"),

        @JsonSubTypes.Type(value = AlertNodeConfig.class, name = "ALERT"),

        @JsonSubTypes.Type(value = StartNodeConfig.class, name = "START")
})
public interface NodeConfig {
    // 공통 필드 있으면 여기
    NodeType nodeType();
    Integer x();
    Integer y();

    default MeasurementType measurementType(){
        return null;
    }
}