package com.nhnacademy.front.processing.service;

import com.nhnacademy.front.processing.client.ProcessingMqttClient;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerUpdateRequest;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttBrokerService {

    private final ProcessingMqttClient processingMqttClient;

    public MqttBrokerInfoDto getBrokerByBuilding(Long buildingId) {
        try {
            return processingMqttClient.getBrokerByBuilding(buildingId);
        } catch (Exception e) {
            return null;
        }
    }

    // 건물 등록 -> 반환된 buildingId를 경로로 받아 여기서 브로커 등록 요청을 만든다.
    public MqttBrokerInfoDto registerBroker(Long buildingId, MqttBrokerRegisterRequest request) {
        MqttBrokerUpdateRequest broker = toCreateRequest(buildingId, request);
        return processingMqttClient.registerBroker(broker);
    }

    public MqttBrokerInfoDto updateBroker(Long buildingId, MqttBrokerRegisterRequest request) {
        MqttBrokerUpdateRequest broker = toCreateRequest(buildingId, request);
        return processingMqttClient.updateBroker(buildingId, broker);
    }

    public void deleteBrokerByBuilding(Long buildingId) {
        processingMqttClient.deleteBrokerByBuilding(buildingId);
    }

    private MqttBrokerUpdateRequest toCreateRequest(Long buildingId, MqttBrokerRegisterRequest request) {
        return new MqttBrokerUpdateRequest(
                buildingId,
                request.serverName(),
                request.brokerUrl(),
                request.username(),
                request.password(),
                request.topic()
        );
    }
}
