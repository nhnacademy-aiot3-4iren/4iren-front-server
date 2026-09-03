package com.nhnacademy.front.processing.dto.mqtt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 먼저 건물을 생성하고 나중에 브로커를 등록할 때 사용하는 dto
public record MqttBrokerUpdateRequest(
        @NotNull(message = "building ID는 필수입니다.")
        Long buildingId,

        @NotBlank(message = "서버 이름은 필수입니다.")
        String serverName,

        @NotBlank(message = "브로커 URL은 필수입니다.")
        String brokerUrl,

        String username,
        String password,

        @NotBlank(message = "토픽은 필수입니다.")
        String topic
) {}