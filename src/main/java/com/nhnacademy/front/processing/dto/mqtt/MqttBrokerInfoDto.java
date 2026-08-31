package com.nhnacademy.front.processing.dto.mqtt;

public record MqttBrokerInfoDto(
        Long id,
        String serverName,
        String brokerUrl,
        String username,
        String password,
        String topic
) {}
