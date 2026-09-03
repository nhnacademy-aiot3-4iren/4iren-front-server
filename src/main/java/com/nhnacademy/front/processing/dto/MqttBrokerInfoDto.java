package com.nhnacademy.front.processing.dto;

public record MqttBrokerInfoDto(
        Long id,
        String serverName,
        String brokerUrl,
        String username,
        String password,
        String topic
) {}
