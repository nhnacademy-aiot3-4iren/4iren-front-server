package com.nhnacademy.front.rule.dto.jsoninfo.action;


import com.nhnacademy.front.rule.dto.jsoninfo.NodeConfig;
import com.nhnacademy.front.rule.enums.AlertType;
import com.nhnacademy.front.rule.enums.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertNodeConfig(

    @NotNull
    NodeType nodeType,

    @NotNull
    Integer x,

    @NotNull
    Integer y,

    @NotBlank
    String alertTitle,

    @NotNull
    AlertType alertType,

    @NotNull
    @Positive
    Integer dedupWindowSec
) implements NodeConfig {

}
