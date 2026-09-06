package com.nhnacademy.front.rule.dto.nodeconfig;

import com.fasterxml.jackson.databind.JsonNode;

public record NodeConfigValidateRequest (
        JsonNode nodeConfig
){
}
