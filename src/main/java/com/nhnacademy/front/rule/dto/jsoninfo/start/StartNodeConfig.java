package com.nhnacademy.front.rule.dto.jsoninfo.start;

import com.nhnacademy.front.rule.dto.jsoninfo.NodeConfig;
import com.nhnacademy.front.rule.enums.NodeType;
import jakarta.validation.constraints.NotNull;

public record StartNodeConfig(
        @NotNull
        NodeType nodeType,

        @NotNull
        Integer x,

        @NotNull
        Integer y
) implements NodeConfig {

}
