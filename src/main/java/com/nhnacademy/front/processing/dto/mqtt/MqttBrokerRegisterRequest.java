package com.nhnacademy.front.processing.dto.mqtt;

import jakarta.validation.constraints.NotBlank;

// 건물을 생성하면서 같이 브로커를 등록할 때 사용하는 브로커
public record MqttBrokerRegisterRequest(
        @NotBlank(message = "서버 이름은 필수입니다.")
        String serverName,

        @NotBlank(message = "브로커 URL은 필수입니다.")
        String brokerUrl,

        String username,
        String password,

        @NotBlank(message = "토픽은 필수입니다.")
        String topic
) {}