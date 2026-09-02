package com.nhnacademy.front.processing.service;

import com.nhnacademy.front.processing.client.ProcessingMqttClient;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerCreateRequest;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
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

    public MqttBrokerInfoDto registerBroker(MqttBrokerCreateRequest request) {
        return processingMqttClient.registerBroker(request);
    }
}
