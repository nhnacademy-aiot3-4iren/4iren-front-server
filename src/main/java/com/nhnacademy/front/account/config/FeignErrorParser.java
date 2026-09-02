package com.nhnacademy.front.account.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getMessage(FeignException e, String defaultMessage) {
        try {
            JsonNode node = objectMapper.readTree(e.contentUTF8());
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ex) {
            log.warn("Error parsing feign exception", ex);
        }
        return defaultMessage;
    }

}
